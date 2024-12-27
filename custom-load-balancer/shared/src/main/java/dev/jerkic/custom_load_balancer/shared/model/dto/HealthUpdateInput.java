package dev.jerkic.custom_load_balancer.shared.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HealthUpdateInput {
  private String serviceId;
  private String serviceName;
  private ServiceHealthInput health;
}
