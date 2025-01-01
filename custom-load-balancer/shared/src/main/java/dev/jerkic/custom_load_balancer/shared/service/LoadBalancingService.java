package dev.jerkic.custom_load_balancer.shared.service;

import dev.jerkic.custom_load_balancer.shared.model.UsedResolvedInstance;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoadBalancingService {
  private final ServiceResolverService serviceResolverService;

  private final ConcurrentHashMap<String, PriorityQueue<UsedResolvedInstance>> cache =
      new ConcurrentHashMap<>();

  public Optional<String> getBestInstanceForBaseHref(String requestedUri) {
    var baseHref = this.getBaseHrefFromURI(requestedUri);
    if (baseHref == null) {
      log.error("No base href foudn for uri {}", requestedUri);
      return Optional.empty();
    }

    var instances = this.cache.computeIfAbsent(baseHref, this::fillCacheForBaseHref);

    var bestInstance = instances.poll();
    if (bestInstance == null) {
      log.error("No instances found for base href {}", baseHref);
      return Optional.empty();
    }
    bestInstance.incrementActiveRequests();

    return Optional.of(bestInstance.getInstance().getAddress());
  }

  @Scheduled(fixedRate = 10_000)
  public void updateCache() {
    this.cache.clear();
  }

  private PriorityQueue<UsedResolvedInstance> fillCacheForBaseHref(String baseHref) {
    var resolvedInstanecs = this.serviceResolverService.resolveForBaseHref(baseHref);
    var queue = new PriorityQueue<UsedResolvedInstance>();
    queue.addAll(resolvedInstanecs.stream().map(UsedResolvedInstance::new).toList());
    return queue;
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
