package dev.jerkic.custom_load_balancer.discovery_server.repository;

import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceModel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface ServiceModelRepository extends CrudRepository<ServiceModel, String> {
  public interface ServiceModelProjection {
    String getId();

    String getServiceName();
  }

  Optional<ServiceModel> findByServiceName(String serviceName);

  // Projected queries
  List<ServiceModelProjection> findAllProjectedBy();

  Optional<ServiceModelProjection> findByServiceNameProjectedBy(String serviceName);
}
