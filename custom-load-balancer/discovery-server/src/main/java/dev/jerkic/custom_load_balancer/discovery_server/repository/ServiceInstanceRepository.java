package dev.jerkic.custom_load_balancer.discovery_server.repository;

import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceInstance;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ServiceInstanceRepository
    extends CrudRepository<ServiceInstance, UUID>,
        PagingAndSortingRepository<ServiceInstance, UUID> {
  List<ServiceInstance> findByServiceId(String serviceId);

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
          SELECT * FROM service_instance
          WHERE service_id = :serviceId AND is_healthy = true
          GROUP BY instance_id
          """)
  Page<ServiceInstance> findByServiceIdAndIsHealthyTrue(UUID serviceId, Pageable pageable);
}
