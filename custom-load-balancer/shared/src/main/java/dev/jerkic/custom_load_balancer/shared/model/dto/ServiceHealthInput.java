package dev.jerkic.custom_load_balancer.shared.model.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceHealthInput {
  private final String serviceName;
  private final boolean isHealthy;
  private final Long numberOfConnections;
  private final Instant timestamp;
}
