package dev.jerkic.custom_load_balancer.client.service;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ActiveRequestsService {
  private AtomicInteger activeRequests = new AtomicInteger(0);

  public void incrementActiveRequests() {
    var curr = this.activeRequests.incrementAndGet();
    log.info("Incremented active requests. Current number of requests: {}", curr);
  }

  public void decrementActiveRequests() {
    var curr = this.activeRequests.decrementAndGet();
    log.info("Decremented active requests. Current number of requests: {}", curr);
  }

  public int getActiveRequests() {
    return this.activeRequests.get();
  }
}
