package dev.jerkic.custom_load_balancer.discovery_server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.jerkic.custom_load_balancer.discovery_server.repository.ServiceInstanceRepository;
import dev.jerkic.custom_load_balancer.discovery_server.repository.ServiceModelRepository;
import dev.jerkic.custom_load_balancer.discovery_server.service.ServiceManagement;
import dev.jerkic.custom_load_balancer.shared.model.dto.RegisterInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.ServiceHealthInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.ServiceInfo;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class ServiceManagementTests {
  @Autowired private ServiceModelRepository serviceModelRepository;
  @Autowired private ServiceInstanceRepository serviceInstanceRepository;
  @Autowired private ServiceManagement serviceManagement;

  @AfterEach
  @Transactional
  public void tearDown() {
    this.serviceInstanceRepository.deleteAll();
    this.serviceModelRepository.deleteAll();
  }

  @Test
  public void testRegisterService() {
    var serviceName = "test-service";
    var registerInput =
        RegisterInput.builder()
            .serviceInfo(this.getServiceInfo(serviceName))
            .serviceHealth(this.getServiceHealth("8090", true, serviceName))
            .build();

    var instanceId = this.serviceManagement.registerService(registerInput);
    assertNotNull(instanceId);

    var instance =
        this.serviceInstanceRepository.findByInstanceId(UUID.fromString(instanceId)).get();

    assertNotNull(instance);
    assertEquals(serviceName, instance.getServiceModel().getServiceName());
    assertEquals("8090", instance.getAddress());

    assertEquals(1, this.serviceModelRepository.count());
    assertEquals(1, this.serviceInstanceRepository.count());
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
