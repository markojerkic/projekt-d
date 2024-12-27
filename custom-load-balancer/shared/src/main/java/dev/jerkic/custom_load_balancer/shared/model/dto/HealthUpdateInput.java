package dev.jerkic.custom_load_balancer.shared.model.dto;

import lombok.Data;

@Data
public class HealthUpdateInput {
  private final String serviceId;
  private final ServiceHealthInput health;
}
