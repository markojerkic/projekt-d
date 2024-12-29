package dev.jerkic.custom_load_balancer.discovery_server.repository;

import dev.jerkic.custom_load_balancer.discovery_server.model.BestInstance;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface BestInstanceRepository
    extends CrudRepository<BestInstance, String>,
        PagingAndSortingRepository<BestInstance, String> {}