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
      value =
          """
          select si.*
          from service_instance si
          join (
              select s.instance_id, max(s.instance_recorded_at) as latest_timestamp
              from service_instance s
              where s.service_model_id = :serviceId
                and s.is_healthy is true
                and strftime('%s', 'now') * 1000 - s.instance_recorded_at
                >= 3*60*1000
              group by s.instance_id
          ) latest
          on si.instance_id = latest.instance_id
          order by si.instance_recorded_at desc, si.number_of_connections asc
          """,
      nativeQuery = true)
  List<ServiceInstance> findLatestForServiceId(@Param("serviceId") UUID serviceId);
}
