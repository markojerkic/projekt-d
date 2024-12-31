package dev.jerkic.custom_load_balancer.client.service;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class ActiveRequestsService {
  private AtomicInteger activeRequests = new AtomicInteger(0);

  public void incrementActiveRequests() {
    this.activeRequests.incrementAndGet();
  }

  public void decrementActiveRequests() {
    this.activeRequests.decrementAndGet();
  }

  public int getActiveRequests() {
    return this.activeRequests.get();
  }
}
