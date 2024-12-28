package dev.jerkic.custom_load_balancer.discovery_server.service;

import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceInstance;
import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceModel;
import dev.jerkic.custom_load_balancer.discovery_server.model.projection.ServiceModelLazyProjection;
import dev.jerkic.custom_load_balancer.discovery_server.repository.ServiceInstanceRepository;
import dev.jerkic.custom_load_balancer.discovery_server.repository.ServiceModelRepository;
import dev.jerkic.custom_load_balancer.shared.model.dto.HealthUpdateInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.RegisterInput;
import dev.jerkic.custom_load_balancer.shared.service.ServiceHealthService;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    UUID serviceId;
    if (!registeredService.isPresent()) {
      var service = ServiceModel.builder().serviceName(serviceInfo.getServiceName()).build();
      serviceId = this.serviceModelRepository.save(service).getId();
    } else {
      serviceId = registeredService.map(ServiceModel::getId).get();
    }

    var instance =
        ServiceInstance.builder()
            .instanceId(UUID.randomUUID().toString())
            // empty shell only containing PK for JPA to connect them
            .serviceModel(ServiceModel.builder().id(serviceId).build())
            .isHealthy(registerInput.getServiceHealth().isHealthy())
            .timestamp(registerInput.getServiceHealth().getTimestamp())
            .address(registerInput.getServiceHealth().getAddress())
            .numberOfConnections(registerInput.getServiceHealth().getNumberOfConnections())
            .build();

    return this.serviceInstanceRepository.save(instance).getInstanceId();
  }

  @Transactional
  @Override
  public void updateHealth(HealthUpdateInput healthUpdateInput) {
    var service =
        this.serviceModelRepository
            .findById(UUID.fromString(healthUpdateInput.getInstanceId()))
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format(
                            "Service with id '%s' not found", healthUpdateInput.getInstanceId())));

    var newInstance =
        this.serviceInstanceRepository.save(
            ServiceInstance.builder()
                // empty shell only containing PK for JPA to connect them
                .serviceModel(ServiceModel.builder().id(service.getId()).build())
                .instanceId(healthUpdateInput.getInstanceId())
                .address(healthUpdateInput.getHealth().getAddress())
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

  /**
   * Return collection of instances. Group results by instanceId, sort by timestamp and for each
   * instaceId, return only the latest instance
   *
   * @param serviceId
   * @return collection of instances
   */
  public Collection<ServiceInstance> getInstacesForService(String serviceId) {
    var instances =
        this.serviceInstanceRepository.findByServiceIdAndIsHealthyTrue(
            UUID.fromString(serviceId),
            PageRequest.of(
                0,
                10,
                Sort.by(Sort.Order.desc("timestamp"), Sort.Order.asc("numberOfConnections"))));

    var groupedByInstanceId =
        instances.stream().collect(Collectors.groupingBy(ServiceInstance::getInstanceId));

    return groupedByInstanceId.values().stream()
        .map(extractLatestInstace())
        .collect(Collectors.toList());
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

  /**
   * Extract latest instance from collection of instances by reducing them to the one with the
   * latest
   *
   * @return function that extracts latest instance
   */
  private Function<List<ServiceInstance>, ServiceInstance> extractLatestInstace() {
    return serviceInstances -> {
      return serviceInstances.stream()
          .filter(this.isOlderThanMinutes(2).negate())
          .reduce(
              (first, second) -> {
                if (first.getTimestamp().isAfter(second.getTimestamp())) {
                  return first;
                } else {
                  return second;
                }
              })
          .get();
    };
  }

  private Predicate<ServiceInstance> isOlderThanMinutes(int minutes) {
    return (instance) -> {
      var minutesAgo = Instant.now().minus(Duration.ofMinutes(minutes));

      return instance.getTimestamp().isBefore(minutesAgo);
    };
  }
}
