package dev.jerkic.custom_load_balancer.discovery_server.repository;

import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceModel;
import dev.jerkic.custom_load_balancer.discovery_server.model.projection.ServiceModelLazyProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface ServiceModelRepository extends CrudRepository<ServiceModel, String> {
  Optional<ServiceModel> findByServiceName(String serviceName);

  // Projected queries
  List<ServiceModelLazyProjection> findAllProjectedBy();

  Optional<ServiceModelLazyProjection> findProjectionByServiceName(String serviceName);

  Optional<ServiceModelLazyProjection> findProjectionById(String id);
}
