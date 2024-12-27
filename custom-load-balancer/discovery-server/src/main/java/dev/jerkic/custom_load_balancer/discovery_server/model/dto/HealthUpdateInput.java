package dev.jerkic.custom_load_balancer.discovery_server.model.dto;

import lombok.Data;

@Data
public class HealthUpdateInput {
  private final String serviceId;
  private final ServiceHealthInput health;
}
