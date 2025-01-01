package dev.jerkic.custom_load_balancer.discovery_server.service;

import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceModel;
import dev.jerkic.custom_load_balancer.discovery_server.repository.ServiceModelRepository;
import dev.jerkic.custom_load_balancer.shared.model.dto.ResolvedInstance;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UrlResolverService {
  private final ServiceModelRepository serviceModelRepository;
  private final ServiceResolverServiceImpl serviceResolverService;
  private final JdbcTemplate jdbcTemplate;

  public List<ResolvedInstance> resolveForBaseHref(String baseHref) {

    var service = this.serviceModelRepository.findByBaseHref(baseHref);
    if (service.isEmpty()) {
      return Collections.emptyList();
    }

    var bestInstances =
        this.serviceResolverService.resolveServiceForServiceId(
            service.map(ServiceModel::getId).get());

    return bestInstances;
  }

  public Optional<ResolvedInstance> findBestInstanceForBaseHref(String requestedUri) {
    var baseHref = this.getBaseHrefFromURI(requestedUri);
    log.debug("Base href for uri {}: {}", requestedUri, baseHref);
    if (baseHref == null) return Optional.empty();

    return this.resolveForBaseHref(baseHref).stream().findFirst();
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
