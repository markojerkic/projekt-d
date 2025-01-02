package dev.jerkic.custom_load_balancer.shared.model;

import dev.jerkic.custom_load_balancer.shared.model.dto.ResolvedInstance;
import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Data;

@Data
public class UsedResolvedInstance {
  private ResolvedInstance instance;
  private Instant usedAt;
  private final ReentrantLock lock = new ReentrantLock();

  public UsedResolvedInstance(ResolvedInstance instance) {
    this.instance = instance;
    this.usedAt = Instant.now();
  }

  public void incrementActiveRequests() {
    this.lock.lock();
    try {
      this.usedAt = Instant.now();
      this.instance.setActiveRequests(this.instance.getActiveRequests() + 1);
    } finally {
      this.lock.unlock();
    }
  }

  public void decrementActiveRequests() {
    this.lock.lock();
    try {
      this.instance.setActiveRequests(this.instance.getActiveRequests() - 1);
    } finally {
      this.lock.unlock();
    }
  }
}
