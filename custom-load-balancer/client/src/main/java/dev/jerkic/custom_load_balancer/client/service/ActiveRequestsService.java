package dev.jerkic.custom_load_balancer.client.service;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ActiveRequestsService {
  private static final AtomicInteger activeRequests = new AtomicInteger(0);

  public void incrementActiveRequests() {
    activeRequests.incrementAndGet();
  }

  public void decrementActiveRequests() {
    activeRequests.decrementAndGet();
  }

  public int getActiveRequests() {
    return activeRequests.get();
  }
}
