package dev.jerkic.custom_load_balancer.discovery_server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceInstance;
import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceModel;
import dev.jerkic.custom_load_balancer.discovery_server.repository.ServiceInstanceRepository;
import dev.jerkic.custom_load_balancer.discovery_server.repository.ServiceModelRepository;
import dev.jerkic.custom_load_balancer.discovery_server.service.ServiceManagement;
import dev.jerkic.custom_load_balancer.discovery_server.service.ServiceResolverServiceImpl;
import dev.jerkic.custom_load_balancer.shared.model.dto.HealthUpdateInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.RegisterInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.ResolvedInstance;
import dev.jerkic.custom_load_balancer.shared.model.dto.ServiceHealthInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.ServiceInfo;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class ServiceManagementTests {
  @Autowired private ServiceModelRepository serviceModelRepository;
  @Autowired private ServiceInstanceRepository serviceInstanceRepository;
  @Autowired private ServiceManagement serviceManagement;
  @Autowired private ServiceResolverServiceImpl serviceResolverServiceImpl;

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

    var instance = this.serviceInstanceRepository.findFirstByInstanceId(instanceId).get();

    assertNotNull(instance);
    assertEquals(serviceName, instance.getServiceModel().getServiceName());
    assertEquals("8090", instance.getAddress());

    assertEquals(1, this.serviceModelRepository.count());
    assertEquals(1, this.serviceInstanceRepository.count());
  }

  @Test
  public void testRegisterExistingService() {
    var serviceName = "test-service";
    var registerInput =
        RegisterInput.builder()
            .serviceInfo(this.getServiceInfo(serviceName))
            .serviceHealth(this.getServiceHealth("8090", true, serviceName))
            .build();

    var instanceId = this.serviceManagement.registerService(registerInput);
    assertNotNull(instanceId);

    var instanceId2 = this.serviceManagement.registerService(registerInput);
    assertNotEquals(instanceId, instanceId2);

    assertEquals(1, this.serviceModelRepository.count());
    assertEquals(2, this.serviceInstanceRepository.count());
  }

  @Test
  public void testUpdateHealthForNonExistingService() {
    var healthUpdate = this.getServiceHealth("8090", true, "test-service");
    assertThrows(
        ResponseStatusException.class,
        () -> {
          this.serviceManagement.updateHealth(
              HealthUpdateInput.builder()
                  .instanceId(UUID.randomUUID().toString())
                  .serviceName(healthUpdate.getServiceName())
                  .health(healthUpdate)
                  .build());
        });

    assertEquals(0, this.serviceModelRepository.count());
    assertEquals(0, this.serviceInstanceRepository.count());
  }

  @Test
  public void testUpdateHeath() {
    // Register
    var serviceName = "test-service";
    var initialHealth = this.getServiceHealth("8090", true, serviceName);
    var registerInput =
        RegisterInput.builder()
            .serviceInfo(this.getServiceInfo(serviceName))
            .serviceHealth(initialHealth)
            .build();

    var instanceId = this.serviceManagement.registerService(registerInput);

    var healthUpdate = this.getServiceHealth("8090", true, serviceName);
    this.serviceManagement.updateHealth(
        HealthUpdateInput.builder()
            .instanceId(instanceId)
            .serviceName(healthUpdate.getServiceName())
            .health(healthUpdate)
            .build());

    assertEquals(1, this.serviceModelRepository.count());
    assertEquals(2, this.serviceInstanceRepository.count());

    var serviceModel =
        this.serviceInstanceRepository
            .findFirstByInstanceId(instanceId)
            .map(ServiceInstance::getServiceModel)
            .get();

    assertNotNull(serviceModel);

    var serviceInstances =
        this.serviceResolverServiceImpl.resolveServiceForServiceId(serviceModel.getId().toString());

    assertEquals(1, serviceInstances.size());
    assertEquals(
        Date.from(healthUpdate.getTimestamp()),
        ((ResolvedInstance) serviceInstances.toArray()[0]).getRecordedAt());
    assertNotEquals(
        Date.from(initialHealth.getTimestamp()),
        ((ResolvedInstance) serviceInstances.toArray()[0]).getRecordedAt());
  }

  @Test
  public void testMultipleInstances() {
    // Register
    var serviceName = "test-service";
    var initialHealth = this.getServiceHealth("8090", true, serviceName);
    var registerInput =
        RegisterInput.builder()
            .serviceInfo(this.getServiceInfo(serviceName))
            .serviceHealth(initialHealth)
            .build();

    var instanceId1 = this.serviceManagement.registerService(registerInput);
    var instanceId2 = this.serviceManagement.registerService(registerInput);

    var healthUpdate1 = this.getServiceHealth("8090", true, serviceName);
    this.serviceManagement.updateHealth(
        HealthUpdateInput.builder()
            .instanceId(instanceId1)
            .serviceName(healthUpdate1.getServiceName())
            .health(healthUpdate1)
            .build());

    var healthUpdate2 = this.getServiceHealth("8090", true, serviceName);
    this.serviceManagement.updateHealth(
        HealthUpdateInput.builder()
            .instanceId(instanceId1)
            .serviceName(healthUpdate2.getServiceName())
            .health(healthUpdate2)
            .build());

    var healthUpdate3 = this.getServiceHealth("8090", true, serviceName);
    this.serviceManagement.updateHealth(
        HealthUpdateInput.builder()
            .instanceId(instanceId2)
            .serviceName(healthUpdate3.getServiceName())
            .health(healthUpdate3)
            .build());

    var serviceModel =
        this.serviceInstanceRepository
            .findFirstByInstanceId(instanceId1)
            .map(ServiceInstance::getServiceModel)
            .get();

    var serviceInstances =
        this.serviceResolverServiceImpl.resolveServiceForServiceId(serviceModel.getId().toString());

    assertEquals(2, serviceInstances.size());
    var instance1 =
        serviceInstances.stream()
            .filter(instance -> instance.getInstanceId().equals(instanceId1))
            .findFirst()
            .get();
    var instance2 =
        serviceInstances.stream()
            .filter(instance -> instance.getInstanceId().equals(instanceId2))
            .findFirst()
            .get();

    assertEquals(Date.from(healthUpdate2.getTimestamp()), instance1.getRecordedAt());
    assertEquals(Date.from(healthUpdate3.getTimestamp()), instance2.getRecordedAt());
  }

  @Test
  public void testInsertTwoServicesWithSameName() {
    var serviceName = "test-service";
    assertThrows(
        Exception.class,
        () -> {
          this.serviceModelRepository.saveAll(
              List.of(
                  ServiceModel.builder().serviceName(serviceName).build(),
                  ServiceModel.builder().serviceName(serviceName).build()));
        });
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
