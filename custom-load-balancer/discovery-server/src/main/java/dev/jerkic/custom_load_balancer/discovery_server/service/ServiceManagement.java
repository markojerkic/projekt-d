package dev.jerkic.custom_load_balancer.discovery_server.service;

import dev.jerkic.custom_load_balancer.discovery_server.util.ServiceHealthComparator;
import dev.jerkic.custom_load_balancer.shared.model.dto.HealthUpdateInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.RegisterInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.ServiceHealthInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.ServiceInfo;
import dev.jerkic.custom_load_balancer.shared.service.ServiceHealthService;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
public class ServiceManagement implements ServiceHealthService {
  // Map of service name to service id to service info
  private final ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceInfo>> services =
      new ConcurrentHashMap<>();
  // Map of service id to health records
  private final ConcurrentHashMap<String, ConcurrentSkipListSet<ServiceHealthInput>> health =
      new ConcurrentHashMap<>();

  @Override
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

    log.info("Registered service {} with id {}", serviceInfo.getServiceName(), serviceId);

    return serviceId;
  }

  @Override
  public void updateHealth(HealthUpdateInput healthUpdateInput) {
    var serviceName =
        this.health.get(healthUpdateInput.getServiceId()).pollFirst().getServiceName();
    var serviceIds = this.services.get(serviceName);
    if (serviceIds == null) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, String.format("Service with id '%s' not found", serviceIds));
    }

    this.health.get(healthUpdateInput.getServiceId()).add(healthUpdateInput.getHealth());

    // Clean up old health records
    var healthList = this.health.get(healthUpdateInput.getServiceId());
    var now = java.time.Instant.now();
    if (healthList.size() > 10) {
      healthList.removeIf(health -> health.getTimestamp().isBefore(now.minusSeconds(60)));
    }

    log.info(
        "Updated health for service {} with id {} - {}",
        this.health.get(healthUpdateInput.getServiceId()).pollFirst().getServiceName(),
        healthUpdateInput.getServiceId(),
        healthUpdateInput);
  }
}
