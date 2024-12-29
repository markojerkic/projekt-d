package dev.jerkic.custom_load_balancer.discovery_server.service;

import dev.jerkic.custom_load_balancer.discovery_server.model.BestInstance;
import dev.jerkic.custom_load_balancer.discovery_server.repository.BestInstanceRepository;
import dev.jerkic.custom_load_balancer.shared.model.dto.ResolvedInstance;
import dev.jerkic.custom_load_balancer.shared.service.ServiceResolverService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServiceResolverServiceImpl implements ServiceResolverService {
  private final BestInstanceRepository bestInstanceRepository;

  @Override
  public List<ResolvedInstance> resolveService(String serviceName) {
    this.bestInstanceRepository
        .findAll(
            this.getBestInstanceSpecification(serviceName),
            Sort.by("serviceInstance.activeHttpRequests"))
        .stream()
        .map(this::mapToResolvedInstance)
        .collect(Collectors.toList());

    return Collections.emptyList();
  }

  @Override
  public Optional<ResolvedInstance> resolveBestInstance(String serviceName) {
    return this.bestInstanceRepository
        .findFirstByServiceInstance_serviceModel_serviceName(serviceName)
        .map(this::mapToResolvedInstance);
  }

  private Specification<BestInstance> getBestInstanceSpecification(String serviceName) {
    return (root, _, criteriaBuilder) -> {
      return criteriaBuilder.equal(root.get("serviceInstance").get("serviceModel").get("serviceName"), serviceName);
    };
  }

  private ResolvedInstance mapToResolvedInstance(BestInstance bestInstance) {
    return ResolvedInstance.builder()
        .instanceId(bestInstance.getServiceInstance().getInstanceId())
        .address(bestInstance.getServiceInstance().getAddress())
        .isHealthy(bestInstance.getServiceInstance().isHealthy())
        .build();
  }
}
