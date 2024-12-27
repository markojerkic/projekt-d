package dev.jerkic.custom_load_balancer.shared.model.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceHealthInput {
  private String serviceName;
  private boolean isHealthy;
  private Long numberOfConnections;
  private Instant timestamp;
}