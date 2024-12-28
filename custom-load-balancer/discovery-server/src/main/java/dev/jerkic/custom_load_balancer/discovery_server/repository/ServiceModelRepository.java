package dev.jerkic.custom_load_balancer.discovery_server.repository;

import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceModel;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ServiceModelRepository
    extends CrudRepository<ServiceModel, UUID>, PagingAndSortingRepository<ServiceModel, UUID> {
  Optional<ServiceModel> findByServiceName(String serviceName);
}
