package dev.jerkic.custom_load_balancer.discovery_server.repository;

import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceModel;
import java.util.Optional;
import java.util.String;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ServiceModelRepository
    extends CrudRepository<ServiceModel, String>, PagingAndSortingRepository<ServiceModel, String> {
  Optional<ServiceModel> findByServiceName(String serviceName);
}
