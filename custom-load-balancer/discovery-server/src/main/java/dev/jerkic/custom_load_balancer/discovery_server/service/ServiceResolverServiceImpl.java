package dev.jerkic.custom_load_balancer.discovery_server.service;

import dev.jerkic.custom_load_balancer.discovery_server.model.BestInstance;
import dev.jerkic.custom_load_balancer.discovery_server.repository.BestInstanceRepository;
import dev.jerkic.custom_load_balancer.discovery_server.repository.ServiceInstanceRepository;
import dev.jerkic.custom_load_balancer.shared.model.dto.ResolvedInstance;
import dev.jerkic.custom_load_balancer.shared.service.ServiceResolverService;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ServiceResolverServiceImpl implements ServiceResolverService {
  private final BestInstanceRepository bestInstanceRepository;
  private final ServiceInstanceRepository serviceInstanceRepository;

  @Override
  public List<ResolvedInstance> resolveService(String serviceName) {
    return this.bestInstanceRepository
        .findAll(
            this.getBestInstanceSpecification(serviceName),
            Sort.by(Order.desc("latestTimestamp"), Order.asc("serviceInstance.activeHttpRequests")))
        .stream()
        .map(this::mapToResolvedInstance)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<ResolvedInstance> resolveBestInstance(String serviceName) {
    return this.bestInstanceRepository
        .findFirstByServiceInstance_serviceModel_serviceName(serviceName)
        .map(this::mapToResolvedInstance);
  }

  public List<ResolvedInstance> resolveServiceForServiceId(String serviceId) {
    return this.bestInstanceRepository
        .findByServiceId(
            serviceId,
            Sort.by(Order.desc("latestTimestamp"), Order.asc("serviceInstance.activeHttpRequests")))
        .stream()
        .map(this::mapToResolvedInstance)
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unused")
  private Specification<BestInstance> getBestInstanceSpecification(String serviceName) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(
            root.get("serviceInstance").get("serviceModel").get("serviceName"), serviceName);
  }

  private ResolvedInstance mapToResolvedInstance(BestInstance bestInstance) {
    return ResolvedInstance.builder()
        .instanceId(bestInstance.getServiceInstance().getInstanceId())
        .address(bestInstance.getServiceInstance().getAddress())
        .isHealthy(bestInstance.getServiceInstance().isHealthy())
        .recordedAt(bestInstance.getLatestTimestamp())
        .build();
  }
}
