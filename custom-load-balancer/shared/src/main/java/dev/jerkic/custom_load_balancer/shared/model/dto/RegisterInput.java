package dev.jerkic.custom_load_balancer.shared.model.dto;

import lombok.Data;

@Data
public class RegisterInput {
  private final ServiceHealthInput serviceHealth;
  private final ServiceInfo serviceInfo;
}
