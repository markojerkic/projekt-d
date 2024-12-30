package dev.jerkic.custom_load_balancer.discovery_server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.jerkic.custom_load_balancer.discovery_server.service.ServiceManagement;
import dev.jerkic.custom_load_balancer.discovery_server.service.ServiceResolverServiceImpl;
import dev.jerkic.custom_load_balancer.shared.model.dto.HealthUpdateInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.RegisterInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.ServiceHealthInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.ServiceInfo;
import jakarta.transaction.Transactional;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ServiceResolverServiceImplTests {
  @Autowired private ServiceManagement serviceManagement;
  @Autowired private ServiceResolverServiceImpl serviceResolverService;

  @Test
  public void testResolveServiceToInstances() {
    // Register
    var serviceName = "test-service";
    var initialHealth = this.getServiceHealth("8090", true, serviceName);
    var registerInput =
        RegisterInput.builder()
            .serviceInfo(this.getServiceInfo(serviceName))
            .serviceHealth(initialHealth)
            .build();

    var instanceId = this.serviceManagement.registerService(registerInput);

    var healthUpdate = this.getServiceHealth("8070", true, serviceName);
    this.serviceManagement.updateHealth(
        HealthUpdateInput.builder()
            .instanceId(instanceId)
            .serviceName(healthUpdate.getServiceName())
            .health(healthUpdate)
            .build());

    var resolvedBestInstances = this.serviceResolverService.resolveService(serviceName);
    assertEquals(1, resolvedBestInstances.size(), "Expected 1 resolved instance");
    assertEquals("8070", resolvedBestInstances.get(0).getAddress());
  }

  @Test
  public void testResolveServiceToInstancesWhenMultipleServices() {

    // False target
    this.serviceManagement.registerService(
        RegisterInput.builder()
            .serviceInfo(this.getServiceInfo("test-2"))
            .serviceHealth(this.getServiceHealth("8020", true, "test-2"))
            .build());

    // Register true target
    var serviceName = "test-service";
    var initialHealth = this.getServiceHealth("8090", true, serviceName);
    var registerInput =
        RegisterInput.builder()
            .serviceInfo(this.getServiceInfo(serviceName))
            .serviceHealth(initialHealth)
            .build();

    var instanceId = this.serviceManagement.registerService(registerInput);

    var healthUpdate = this.getServiceHealth("8070", true, serviceName);
    this.serviceManagement.updateHealth(
        HealthUpdateInput.builder()
            .instanceId(instanceId)
            .serviceName(healthUpdate.getServiceName())
            .health(healthUpdate)
            .build());

    var resolvedBestInstances = this.serviceResolverService.resolveService(serviceName);
    assertEquals(1, resolvedBestInstances.size(), "Expected 1 resolved instance");
    assertEquals("8070", resolvedBestInstances.get(0).getAddress());
  }

  @Test
  public void testMultipleInstances() {
    // Register true target
    var serviceName = "test-service";

    var registerInput1 =
        RegisterInput.builder()
            .serviceInfo(this.getServiceInfo(serviceName))
            .serviceHealth(this.getServiceHealth("8070", true, serviceName))
            .build();
    registerInput1.getServiceHealth().setTimestamp(Instant.now().minusSeconds(100));
    var instanceId1 = this.serviceManagement.registerService(registerInput1);

    var instanceId2 =
        this.serviceManagement.registerService(
            RegisterInput.builder()
                .serviceInfo(this.getServiceInfo(serviceName))
                .serviceHealth(this.getServiceHealth("8030", true, serviceName))
                .build());

    var resolvedBestInstances = this.serviceResolverService.resolveService(serviceName);
    assertEquals(2, resolvedBestInstances.size(), "Expected 2 resolved instance");
    assertEquals(
        instanceId2,
        resolvedBestInstances.get(0).getInstanceId(),
        "Expected instanceId2 as better since recorded after instanceId1");
    assertEquals(
        instanceId1,
        resolvedBestInstances.get(1).getInstanceId(),
        "Expected instanceId1 as worse since recorded before instanceId2");
  }

  @Test()
  public void testMultipleInstancesWithDifferentActiveConnections() {
    // Register true target
    var serviceName = "test-service";
    var initialHealth = this.getServiceHealth("8090", true, serviceName);
    var registerInput =
        RegisterInput.builder()
            .serviceInfo(this.getServiceInfo(serviceName))
            .serviceHealth(initialHealth)
            .build();

    var instanceTimestamp = Instant.now();

    var instanceId1 = this.serviceManagement.registerService(registerInput);
    var healthUpdate1 = this.getServiceHealth("8070", true, serviceName);
    healthUpdate1.setTimestamp(instanceTimestamp);
    this.serviceManagement.updateHealth(
        HealthUpdateInput.builder()
            .instanceId(instanceId1)
            .serviceName(healthUpdate1.getServiceName())
            .health(healthUpdate1)
            .build());

    var instanceId2 = this.serviceManagement.registerService(registerInput);
    var healthUpdate2 = this.getServiceHealth("8030", true, serviceName);
    healthUpdate2.setTimestamp(instanceTimestamp);
    healthUpdate2.setNumberOfConnections(3l);
    this.serviceManagement.updateHealth(
        HealthUpdateInput.builder()
            .instanceId(instanceId2)
            .serviceName(healthUpdate2.getServiceName())
            .health(healthUpdate2)
            .build());

    var resolvedBestInstances = this.serviceResolverService.resolveService(serviceName);
    assertEquals(2, resolvedBestInstances.size(), "Expected 2 resolved instance");
    assertEquals(
        "8070",
        resolvedBestInstances.get(0).getAddress(),
        "Expected 8070 as better since less connections");
    assertEquals(
        "8030",
        resolvedBestInstances.get(1).getAddress(),
        "Expected 8030 as worse since more connectinos");
  }

  private ServiceHealthInput getServiceHealth(String port, boolean isHealthy, String serviceName) {
    return ServiceHealthInput.builder()
        .address(port)
        .isHealthy(isHealthy)
        .serviceName(serviceName)
        .numberOfConnections(0l)
        .timestamp(Instant.now())
        .build();
  }

  private ServiceInfo getServiceInfo(String serviceName) {
    return ServiceInfo.builder().serviceName(serviceName).serviceHealthCheckUrl("/health").build();
  }
}
