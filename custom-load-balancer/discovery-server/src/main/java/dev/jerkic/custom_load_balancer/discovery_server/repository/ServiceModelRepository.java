package dev.jerkic.custom_load_balancer.discovery_server.repository;

import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceModelRepository extends JpaRepository<ServiceModel, String> {
  Optional<ServiceModel> findByServiceName(String serviceName);

  Optional<ServiceModel> findByBaseHref(String baseHref);
}
