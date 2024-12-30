package dev.jerkic.custom_load_balancer.discovery_server.service;

import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceModel;
import dev.jerkic.custom_load_balancer.discovery_server.repository.BestInstanceRepository;
import dev.jerkic.custom_load_balancer.discovery_server.repository.ServiceModelRepository;
import dev.jerkic.custom_load_balancer.shared.model.dto.ResolvedInstance;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LoadBalancingService {
  private final BestInstanceRepository bestInstanceRepository;
  private final ServiceModelRepository serviceModelRepository;
  private final ServiceResolverServiceImpl serviceResolverService;
  private final JdbcTemplate jdbcTemplate;
  private final RestTemplate restTemplate;

  public ResponseEntity<?> proxyRequest(HttpServletRequest request) {
    var requestedPath = request.getRequestURI();
    var bestInstance = this.findBestInstanceForBaseHref(requestedPath);
    if (bestInstance.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok("Nema još ništa");
  }

  public Optional<ResolvedInstance> findBestInstanceForBaseHref(String requestedUri) {
    var baseHref = this.getBaseHrefFromURI(requestedUri);
    if (baseHref == null) return Optional.empty();

    var service = this.serviceModelRepository.findByBaseHref(baseHref);
    if (service.isEmpty()) {
      return Optional.empty();
    }

    var bestInstances =
        this.serviceResolverService.resolveServiceForServiceId(
            service.map(ServiceModel::getId).get());

    var bestInstance = bestInstances.stream().findFirst();
    if (bestInstance.isPresent()) {
      this.incrementActiveRequests(bestInstance.get());
    }

    return bestInstance;
  }

  private void incrementActiveRequests(ResolvedInstance instance) {
    this.jdbcTemplate.update(
        "UPDATE service_instance SET active_http_requests = active_http_requests + 1 WHERE"
            + " instance_id = ?",
        instance.getInstanceId());
  }

  private String getBaseHrefFromURI(String requestedUri) {
    if (requestedUri == null) return null;

    return requestedUri.split("/")[1];
  }
}
