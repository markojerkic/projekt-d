package dev.jerkic.custom_load_balancer.discovery_server.service;

import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceInstance;
import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceModel;
import dev.jerkic.custom_load_balancer.discovery_server.model.projection.ServiceModelLazyProjection;
import dev.jerkic.custom_load_balancer.discovery_server.repository.ServiceInstanceRepository;
import dev.jerkic.custom_load_balancer.discovery_server.repository.ServiceModelRepository;
import dev.jerkic.custom_load_balancer.shared.model.dto.HealthUpdateInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.RegisterInput;
import dev.jerkic.custom_load_balancer.shared.service.ServiceHealthService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServiceManagement implements ServiceHealthService {
  private final ServiceModelRepository serviceModelRepository;
  private final ServiceInstanceRepository serviceInstanceRepository;

  @Override
  public String registerService(RegisterInput registerInput) {

    var serviceInfo = registerInput.getServiceInfo();
    var registeredService =
        this.serviceModelRepository.findByServiceName(serviceInfo.getServiceName());

    String serviceId;
    if (!registeredService.isPresent()) {
      var service = ServiceModel.builder().serviceName(serviceInfo.getServiceName()).build();
      this.serviceModelRepository.save(service).getId();
      serviceId = service.getId();
    } else {
      serviceId = registeredService.map(ServiceModel::getId).get();
    }

    var instance =
        ServiceInstance.builder()
            .instanceId(UUID.randomUUID().toString())
            .serviceId(serviceId)
            .isHealthy(registerInput.getServiceHealth().isHealthy())
            .timestamp(registerInput.getServiceHealth().getTimestamp())
            .numberOfConnections(registerInput.getServiceHealth().getNumberOfConnections())
            .build();

    return this.serviceInstanceRepository.save(instance).getInstanceId();
  }

  @Transactional
  @Override
  public void updateHealth(HealthUpdateInput healthUpdateInput) {
    var service =
        this.serviceModelRepository
            .findById(healthUpdateInput.getInstanceId())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format(
                            "Service with id '%s' not found", healthUpdateInput.getInstanceId())));

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

  public List<ServiceModelLazyProjection> getServices() {
    return this.serviceModelRepository.findAllProjectedBy();
  }

  public Page<ServiceInstance> getInstacesForService(String serviceId) {
    return this.serviceInstanceRepository.findByServiceId(
        serviceId,
        PageRequest.of(
            0, 10, Sort.by(Sort.Order.desc("timestamp"), Sort.Order.asc("numberOfConnections"))));
  }

  public ServiceModelLazyProjection getServiceInfo(String serviceId) {
    return this.serviceModelRepository
        .findProjectionById(serviceId)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format("Service with id '%s' not found", serviceId)));
  }
}
