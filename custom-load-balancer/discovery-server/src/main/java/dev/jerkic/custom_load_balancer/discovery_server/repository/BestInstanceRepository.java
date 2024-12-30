package dev.jerkic.custom_load_balancer.discovery_server.repository;

import dev.jerkic.custom_load_balancer.discovery_server.model.BestInstance;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BestInstanceRepository
    extends JpaRepository<BestInstance, String>, JpaSpecificationExecutor<BestInstance> {
  List<BestInstance> findByServiceId(String serviceId);

  // Find by serviceInstce.serviceMode.serviceName
  List<BestInstance> findByServiceInstance_serviceModel_serviceName(String serviceName);

  Optional<BestInstance> findFirstByServiceInstance_serviceModel_serviceName(String serviceName);
}
