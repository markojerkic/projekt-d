package dev.jerkic.custom_load_balancer.discovery_server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceModel;
import dev.jerkic.custom_load_balancer.discovery_server.repository.ServiceModelRepository;
import dev.jerkic.custom_load_balancer.discovery_server.service.ServiceManagement;
import dev.jerkic.custom_load_balancer.discovery_server.service.ServiceResolverServiceImpl;
import dev.jerkic.custom_load_balancer.shared.model.dto.HealthUpdateInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.RegisterInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.ServiceHealthInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.ServiceInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ServiceResolverServiceImplTests {
  @Autowired private ServiceModelRepository serviceModelRepository;
  @Autowired private ServiceManagement serviceManagement;
  @Autowired private ServiceResolverServiceImpl serviceResolverService;

  @TestConfiguration
  static class TestConfig {
    @Bean
    @Primary
    public HttpServletRequest mockRequest() {
      var request = Mockito.mock(HttpServletRequest.class);

      Mockito.when(request.getRemoteAddr()).thenReturn("localhost");
      Mockito.when(request.getScheme()).thenReturn("http");
      Mockito.when(request.getHeader(Mockito.any())).thenReturn(null);

      return request;
    }
  }

  @Test
  public void testTwoInstancesWithSamePort() {
    // Register
    var serviceName = "test-service";

    var initialHealth1 = this.getServiceHealth("8090", true, serviceName);
    var registerInput1 =
        RegisterInput.builder()
            .serviceInfo(this.getServiceInfo(serviceName))
            .serviceHealth(initialHealth1)
            .build();
    var instanceId1 = this.serviceManagement.registerService(registerInput1);

    var initialHealth2 = this.getServiceHealth("8090", true, serviceName);
    var registerInput2 =
        RegisterInput.builder()
            .serviceInfo(this.getServiceInfo(serviceName))
            .serviceHealth(initialHealth2)
            .build();
    registerInput2.getServiceHealth().setTimestamp(Instant.now().minusSeconds(100));
    var instanceId2 = this.serviceManagement.registerService(registerInput2);

    assertEquals(
        instanceId1,
        instanceId2,
        "Two instace registrations with same addr and port should have same instanceId");

    var resolvedBestInstances = this.serviceResolverService.resolveService(serviceName);
    assertEquals(
        1,
        resolvedBestInstances.size(),
        "Two instances with same addr/port should resolve to one instance");
    assertEquals("http://localhost:8090", resolvedBestInstances.get(0).getAddress());
    assertEquals(instanceId1, resolvedBestInstances.get(0).getInstanceId());
  }

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
    assertEquals("http://localhost:8070", resolvedBestInstances.get(0).getAddress());
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
    assertEquals("http://localhost:8070", resolvedBestInstances.get(0).getAddress());
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

    var instanceInputTime = Instant.now();
    var registerInput1 =
        RegisterInput.builder()
            .serviceInfo(this.getServiceInfo(serviceName))
            .serviceHealth(this.getServiceHealth("8070", true, serviceName))
            .build();
    registerInput1.getServiceHealth().setNumberOfConnections(1l);
    registerInput1.getServiceHealth().setTimestamp(instanceInputTime);
    var instanceId1 = this.serviceManagement.registerService(registerInput1);

    var registerInput2 =
        RegisterInput.builder()
            .serviceInfo(this.getServiceInfo(serviceName))
            .serviceHealth(this.getServiceHealth("8070", true, serviceName))
            .build();
    registerInput2.getServiceHealth().setNumberOfConnections(2l);
    registerInput2.getServiceHealth().setTimestamp(instanceInputTime);
    var instanceId2 = this.serviceManagement.registerService(registerInput2);

    var resolvedBestInstances = this.serviceResolverService.resolveService(serviceName);
    assertEquals(2, resolvedBestInstances.size(), "Expected 2 resolved instance");
    assertEquals(
        instanceId1,
        resolvedBestInstances.get(0).getInstanceId(),
        "Expected instanceId1 to be better since it has less connections");
    assertEquals(
        instanceId2,
        resolvedBestInstances.get(1).getInstanceId(),
        "Expected instanceId1 to be better since it has less connections");
  }

  @Test
  public void testResolveByServiceId() {
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

    var serviceId =
        this.serviceModelRepository.findByServiceName(serviceName).map(ServiceModel::getId).get();

    var resolvedBestInstances = this.serviceResolverService.resolveServiceForServiceId(serviceId);
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

  private ServiceHealthInput getServiceHealth(String port, boolean isHealthy, String serviceName) {
    return ServiceHealthInput.builder()
        .serverPort(port)
        .isHealthy(isHealthy)
        .serviceName(serviceName)
        .numberOfConnections(0l)
        .timestamp(Instant.now())
        .build();
  }

  private ServiceInfo getServiceInfo(String serviceName) {
    return ServiceInfo.builder().serviceName(serviceName).baseHref("/" + serviceName).build();
  }
}
