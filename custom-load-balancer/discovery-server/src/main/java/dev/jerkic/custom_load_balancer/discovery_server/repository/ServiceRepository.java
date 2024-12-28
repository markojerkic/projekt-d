package dev.jerkic.custom_load_balancer.discovery_server.repository;

import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceModel;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface ServiceRepository extends CrudRepository<ServiceModel, String> {
  Optional<ServiceModel> findByServiceName(String serviceName);
}
