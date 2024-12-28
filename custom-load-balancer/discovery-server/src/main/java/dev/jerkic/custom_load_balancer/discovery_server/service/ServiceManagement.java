package dev.jerkic.custom_load_balancer.discovery_server.service;

import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceInstance;
import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceModel;
import dev.jerkic.custom_load_balancer.discovery_server.repository.ServiceInstanceRepository;
import dev.jerkic.custom_load_balancer.discovery_server.repository.ServiceRepository;
import dev.jerkic.custom_load_balancer.shared.model.dto.HealthUpdateInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.RegisterInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.ServiceHealthInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.ServiceInfo;
import dev.jerkic.custom_load_balancer.shared.service.ServiceHealthService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServiceManagement implements ServiceHealthService {
  private final ServiceRepository serviceModelRepository;
  private final ServiceInstanceRepository serviceInstanceRepository;

  // Map of service name to service id to service info
  private final ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceInfo>> services =
      new ConcurrentHashMap<>();
  // Map of service id to health records
  private final ConcurrentHashMap<String, ConcurrentSkipListSet<ServiceHealthInput>> health =
      new ConcurrentHashMap<>();

  @Override
  public String registerService(RegisterInput registerInput) {
    var serviceInfo = registerInput.getServiceInfo();

    var service = ServiceModel.builder().serviceName(serviceInfo.getServiceName()).build();
    return this.serviceModelRepository.save(service).getId();
  }

  @Transactional
  @Override
  public void updateHealth(HealthUpdateInput healthUpdateInput) {
    var service =
        this.serviceModelRepository
            .findById(healthUpdateInput.getServiceId())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format(
                            "Service with id '%s' not found", healthUpdateInput.getServiceId())));

    var newInstance =
        this.serviceInstanceRepository.save(
            ServiceInstance.builder()
                .timestamp(healthUpdateInput.getHealth().getTimestamp())
                .numberOfConnections(healthUpdateInput.getHealth().getNumberOfConnections())
                .isHealthy(healthUpdateInput.getHealth().isHealthy())
                .build());
    service.getInstances().add(newInstance);

    this.serviceModelRepository.save(service);
    log.info(
        "Health updated for service '{}' with health {}", service.getServiceName(), newInstance);
  }

  public Iterable<ServiceModel> getServices() {
    return this.serviceModelRepository.findAll();
  }
}
