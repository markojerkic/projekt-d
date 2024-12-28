package dev.jerkic.custom_load_balancer.discovery_server.service;

import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceInstance;
import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceModel;
import dev.jerkic.custom_load_balancer.discovery_server.repository.ServiceInstanceRepository;
import dev.jerkic.custom_load_balancer.discovery_server.repository.ServiceModelRepository;
import dev.jerkic.custom_load_balancer.shared.model.dto.HealthUpdateInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.RegisterInput;
import dev.jerkic.custom_load_balancer.shared.service.ServiceHealthService;
import java.sql.Date;
import java.util.Collection;
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
            .address(registerInput.getServiceHealth().getAddress())
            .numberOfConnections(registerInput.getServiceHealth().getNumberOfConnections())
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
                .address(healthUpdateInput.getHealth().getAddress())
                .instanceRecordedAt(Date.from(healthUpdateInput.getHealth().getTimestamp()))
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

  /**
   * Return collection of instances. Group results by instanceId, sort by timestamp and for each
   * instaceId, return only the latest instance
   *
   * @param serviceId
   * @return collection of instances
   */
  public Collection<ServiceInstance> getInstacesForService(String serviceId) {
    return this.jdbcTemplate.query(
        """
        select si.*
        from service_instance si
        join (
            select s.entry_id, max(s.instance_recorded_at) as latest_timestamp
            from service_instance s
            where s.service_model_id = :serviceId
              and s.is_healthy is true
              and strftime('%s', 'now') * 1000 - s.instance_recorded_at
              <= 3*60*1000
            group by s.instance_id
        ) latest
        on si.entry_id = latest.entry_id
        order by si.instance_recorded_at desc, si.number_of_connections asc
        """,
        (rs, _) -> {
          return ServiceInstance.builder()
              .entryId(rs.getString("entry_id"))
              .instanceId(rs.getString("instance_id"))
              .isHealthy(rs.getBoolean("is_healthy"))
              .address(rs.getString("address"))
              .numberOfConnections(rs.getLong("number_of_connections"))
              .instanceRecordedAt(rs.getDate("instance_recorded_at"))
              .build();
        },
        serviceId);
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
}
