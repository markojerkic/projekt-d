package dev.jerkic.custom_load_balancer.discovery_server.repository;

import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceInstance;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ServiceInstanceRepository
    extends CrudRepository<ServiceInstance, String>,
        PagingAndSortingRepository<ServiceInstance, String> {
  List<ServiceInstance> findByServiceId(String serviceId);

  /**
   * Find all instances of a service which are healthy
   *
   * @param serviceId service id
   * @return page of healthy instances
   */
  Page<ServiceInstance> findByServiceId(String serviceId, Pageable pageable);
}
