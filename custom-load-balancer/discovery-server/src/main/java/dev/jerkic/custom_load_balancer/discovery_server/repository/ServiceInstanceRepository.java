package dev.jerkic.custom_load_balancer.discovery_server.repository;

import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceInstance;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface ServiceInstanceRepository
    extends CrudRepository<ServiceInstance, String>,
        PagingAndSortingRepository<ServiceInstance, String> {
  List<ServiceInstance> findByServiceModel_id(String serviceId);

  Optional<ServiceInstance> findFirstByInstanceId(String instanceId);

  @Query(
      """
        SELECT si.* FROM ServiceInstance si
        JOIN  BestInstance bi ON si.entryId = bi.entryId
        WHERE bi.serviceId = :serviceId
        ORDER BY si.instanceRecordedAt DESC, si.activeHttpRequests
      """)
  List<ServiceInstance> findByServiceIdGroupedByInstanceId(@Param("serviceId") String serviceId);
}
