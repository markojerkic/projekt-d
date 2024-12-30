package dev.jerkic.custom_load_balancer.discovery_server.service;

import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceInstance;
import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceModel;
import dev.jerkic.custom_load_balancer.discovery_server.repository.ServiceInstanceRepository;
import dev.jerkic.custom_load_balancer.discovery_server.repository.ServiceModelRepository;
import dev.jerkic.custom_load_balancer.shared.model.dto.HealthUpdateInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.RegisterInput;
import dev.jerkic.custom_load_balancer.shared.service.ServiceHealthService;
import jakarta.servlet.http.HttpServletRequest;
import java.sql.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServiceManagement implements ServiceHealthService {
  private final ServiceModelRepository serviceModelRepository;
  private final ServiceInstanceRepository serviceInstanceRepository;
  private final JdbcTemplate jdbcTemplate;
  private final HttpServletRequest request;

  @Override
  @Transactional
  public String registerService(RegisterInput registerInput) {

    var serviceInfo = registerInput.getServiceInfo();
    var registeredService =
        this.serviceModelRepository.findByServiceName(serviceInfo.getServiceName());

    ServiceModel serviceModel;
    if (!registeredService.isPresent()) {
      var service =
          ServiceModel.builder()
              .id(UUID.randomUUID().toString())
              .serviceName(serviceInfo.getServiceName())
              .baseHref(serviceInfo.getBaseHref())
              .build();
      serviceModel = this.serviceModelRepository.save(service);
    } else {
      serviceModel = registeredService.get();
    }

    var instance =
        ServiceInstance.builder()
            .entryId(UUID.randomUUID().toString())
            .instanceId(UUID.randomUUID().toString())
            // empty shell only containing PK for JPA to connect them
            .serviceModel(serviceModel)
            .isHealthy(registerInput.getServiceHealth().isHealthy())
            .instanceRecordedAt(Date.from(registerInput.getServiceHealth().getTimestamp()))
            .address(this.buildInstanceAddress(registerInput.getServiceHealth().getServerPort()))
            .activeHttpRequests(registerInput.getServiceHealth().getNumberOfConnections())
            .build();

    return this.serviceInstanceRepository.save(instance).getInstanceId().toString();
  }

  @Transactional
  @Override
  public void updateHealth(HealthUpdateInput healthUpdateInput) {
    var service =
        this.serviceInstanceRepository
            .findFirstByInstanceId(healthUpdateInput.getInstanceId())
            .map(ServiceInstance::getServiceModel)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format(
                            "Service with instanceId '%s' not found",
                            healthUpdateInput.getInstanceId())));

    var newInstance =
        this.serviceInstanceRepository.save(
            ServiceInstance.builder()
                // empty shell only containing PK for JPA to connect them
                .entryId(UUID.randomUUID().toString())
                // .serviceModel(ServiceModel.builder().id(service.getId()).build())
                .serviceModel(service)
                .instanceId(healthUpdateInput.getInstanceId())
                .address(this.buildInstanceAddress(healthUpdateInput.getHealth().getServerPort()))
                .instanceRecordedAt(Date.from(healthUpdateInput.getHealth().getTimestamp()))
                .activeHttpRequests(healthUpdateInput.getHealth().getNumberOfConnections())
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

  public ServiceModel getServiceInfo(String serviceId) {
    return this.serviceModelRepository
        .findById(serviceId)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format("Service with id '%s' not found", serviceId)));
  }

  // Every 3 minutes, cleanup all instances older than 3 mins
  @Scheduled(fixedRate = 3 * 60 * 1000)
  @Transactional
  public void cleanOldInstances() {
    this.jdbcTemplate.update(
        """
        delete from service_instance where
        strftime('%s', 'now') * 1000 - service_instance.instance_recorded_at >= 3*60*1000;
        """);
  }

  private String buildInstanceAddress(String port) {
    var remoteHost = this.request.getRemoteHost();
    var protocol = this.request.getProtocol();
    return String.format("%s://%s:%s", protocol, remoteHost, port);
  }
}
