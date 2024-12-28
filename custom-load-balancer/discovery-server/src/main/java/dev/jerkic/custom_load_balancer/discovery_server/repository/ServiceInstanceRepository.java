package dev.jerkic.custom_load_balancer.discovery_server.repository;

import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceInstance;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface ServiceInstanceRepository
    extends CrudRepository<ServiceInstance, UUID>,
        PagingAndSortingRepository<ServiceInstance, UUID> {
  List<ServiceInstance> findByServiceModel_id(UUID serviceId);

  Optional<ServiceInstance> findByInstanceId(UUID instanceId);

  /**
   * Find all instances of a service which are healthy. Return only the latest instance, grouped by
   * instanceId
   *
   * @param serviceId service id
   * @return page of healthy instances
   */
  @Query(
      """
      select si
      from ServiceInstance si
      join (
          select s.instanceId as instanceId, max(s.instanceRecordedAt) as latestTimestamp
          from ServiceInstance s
          where s.serviceModel.id = :serviceId
            and s.isHealthy = true
            and (current_timestamp - s.instanceRecordedAt >= 3 * 60 * 1000)
          group by s.instanceId
      ) latest
      on si.instanceId = latest.instanceId
      order by si.instanceRecordedAt desc, si.numberOfConnections asc
      """)
  List<ServiceInstance> findLatestForServiceId(@Param("serviceId") UUID serviceId);
}
