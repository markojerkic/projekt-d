package dev.jerkic.custom_load_balancer.discovery_server.service;

import dev.jerkic.custom_load_balancer.discovery_server.model.BestInstance;
import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceModel;
import dev.jerkic.custom_load_balancer.discovery_server.repository.BestInstanceRepository;
import dev.jerkic.custom_load_balancer.discovery_server.repository.ServiceModelRepository;
import dev.jerkic.custom_load_balancer.discovery_server.util.Constants;
import dev.jerkic.custom_load_balancer.shared.model.dto.ResolvedInstance;
import dev.jerkic.custom_load_balancer.shared.service.ServiceResolverService;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ServiceResolverServiceImpl implements ServiceResolverService {
  private final BestInstanceRepository bestInstanceRepository;
  private final ServiceModelRepository serviceModelRepository;

  @Override
  public List<ResolvedInstance> resolveForBaseHref(String baseHref) {
    var service = this.serviceModelRepository.findByBaseHref(baseHref);
    if (service.isEmpty()) {
      return Collections.emptyList();
    }

    var bestInstances = this.resolveServiceForServiceId(service.map(ServiceModel::getId).get());

    return bestInstances;
  }

  @Override
  public Optional<ResolvedInstance> resolveBestInstanceForBaseHref(String requestedUri) {
    var baseHref = this.getBaseHrefFromURI(requestedUri);
    log.debug("Base href for uri {}: {}", requestedUri, baseHref);
    if (baseHref == null) return Optional.empty();

    return this.resolveForBaseHref(baseHref).stream().findFirst();
  }

  @Override
  public List<ResolvedInstance> resolveService(String serviceName) {
    return this.bestInstanceRepository
        .findAll(this.getBestInstanceSpecification(serviceName), Constants.SORT_BEST_INSTANCE)
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
        .findByServiceId(serviceId, Constants.SORT_BEST_INSTANCE)
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
        .baseBref(bestInstance.getServiceInstance().getServiceModel().getBaseHref())
        .build();
  }

  private String getBaseHrefFromURI(String requestedUri) {
    if (requestedUri == null) {
      return null;
    }

    if (!requestedUri.startsWith("/")) {
      try {
        requestedUri = new URI(requestedUri).getPath();
      } catch (URISyntaxException e) {
        log.error("Error parsing uri", e);
        return null;
      }
    }

    return "/" + requestedUri.split("/")[1];
  }
}
