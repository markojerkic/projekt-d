package dev.jerkic.custom_load_balancer.discovery_server.service;

import dev.jerkic.custom_load_balancer.discovery_server.model.dto.HealthUpdateInput;
import dev.jerkic.custom_load_balancer.discovery_server.model.dto.RegisterInput;
import dev.jerkic.custom_load_balancer.discovery_server.model.dto.ServiceHealthInput;
import dev.jerkic.custom_load_balancer.discovery_server.model.dto.ServiceInfo;
import dev.jerkic.custom_load_balancer.discovery_server.util.ServiceHealthComparator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ServiceManagement {
  private final ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceInfo>> services =
      new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, ConcurrentSkipListSet<ServiceHealthInput>> health =
      new ConcurrentHashMap<>();

  /**
   * Registers a service with the discovery server.
   *
   * @param registerInput The service to register.
   * @return The id of the registered service. Used to update the health of the service.
   */
  public String registerService(RegisterInput registerInput) {
    var serviceInfo = registerInput.getServiceInfo();

    services.putIfAbsent(serviceInfo.getServiceName(), new ConcurrentHashMap<>());

    var serviceId = UUID.randomUUID().toString();
    services.get(serviceInfo.getServiceName()).put(serviceId, serviceInfo);

    var healthList =
        new ConcurrentSkipListSet<ServiceHealthInput>(ServiceHealthComparator.getInstance());
    healthList.add(
        ServiceHealthInput.builder()
            .serviceName(serviceInfo.getServiceName())
            .timestamp(java.time.Instant.now())
            .isHealthy(true)
            .build());

    health.put(serviceId, healthList);
    return serviceId;
  }

  /**
   * Updates the health of a service.
   *
   * @param healthUpdateInput The health update input.
   */
  public void updateHealth(HealthUpdateInput healthUpdateInput) {
    var serviceId = this.services.get(healthUpdateInput.getServiceId());
    if (serviceId == null) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, String.format("Service with id '%s' not found", serviceId));
    }

    health.get(healthUpdateInput.getServiceId()).add(healthUpdateInput.getHealth());
  }
}
