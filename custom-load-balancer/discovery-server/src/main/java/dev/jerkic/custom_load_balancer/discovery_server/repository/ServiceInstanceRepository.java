package dev.jerkic.custom_load_balancer.discovery_server.repository;

import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceInstance;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ServiceInstanceRepository
    extends JpaRepository<ServiceInstance, String>, JpaSpecificationExecutor<ServiceInstance> {
  List<ServiceInstance> findByServiceModel_id(String serviceId);

  Optional<ServiceInstance> findFirstByInstanceId(String instanceId);
}
