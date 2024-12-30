package dev.jerkic.custom_load_balancer.discovery_server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.jerkic.custom_load_balancer.discovery_server.repository.ServiceInstanceRepository;
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
  @Autowired private ServiceInstanceRepository serviceInstanceRepository;
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
