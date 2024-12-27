package dev.jerkic.custom_load_balancer.discovery_server.model.dto;

import lombok.Data;

@Data
public class RegisterInput {
  private final ServiceHealthInput serviceHealth;
  private final ServiceInfo serviceInfo;
}
