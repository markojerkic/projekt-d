package dev.jerkic.custom_load_balancer.shared.service;

import dev.jerkic.custom_load_balancer.shared.model.InstaceUriWithId;
import dev.jerkic.custom_load_balancer.shared.model.UsedResolvedInstance;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
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
  private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

  public Optional<InstaceUriWithId> getBestInstanceForBaseHref(String requestedUri) {
    var baseHref = this.getBaseHrefFromURI(requestedUri);
    if (baseHref == null) {
      log.error("No base href found for uri {}", requestedUri);
      return Optional.empty();
    }

    var instances = this.cache.computeIfAbsent(baseHref, this::fillCacheForBaseHref);
    @SuppressWarnings("unused")
    var lock = this.locks.computeIfAbsent(baseHref, k -> new ReentrantLock());

    lock.lock();
    try {
      var bestInstance = instances.poll();
      if (bestInstance == null) {
        log.error("No instances found for base href {}", baseHref);
        return Optional.empty();
      }
      bestInstance.incrementActiveRequests();
      instances.add(bestInstance);
      return Optional.of(
          new InstaceUriWithId(
              bestInstance.getInstance().getAddress(), bestInstance.getInstance().getInstanceId()));
    } finally {
      lock.unlock();
    }
  }

  @Scheduled(fixedRate = 10_000)
  public void clearCache() {
    log.info("Clearing cache");
    this.cache.clear();
  }

  @Scheduled(fixedRate = 300_000)
  public void clearLocks() {
    log.info("Clearing locks");
    this.locks.clear();
  }

  private PriorityQueue<UsedResolvedInstance> fillCacheForBaseHref(String baseHref) {
    log.info("Filling cache for base href {}", baseHref);
    var resolvedInstanecs = this.serviceResolverService.resolveForBaseHref(baseHref);
    var queue = new PriorityQueue<UsedResolvedInstance>();
    queue.addAll(resolvedInstanecs.stream().map(UsedResolvedInstance::new).toList());
    log.info("Done filling cahce with {} instances", resolvedInstanecs.size());
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
