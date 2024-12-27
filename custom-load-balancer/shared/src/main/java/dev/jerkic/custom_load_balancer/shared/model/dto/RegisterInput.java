package dev.jerkic.custom_load_balancer.shared.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterInput {
  private final ServiceHealthInput serviceHealth;
  private final ServiceInfo serviceInfo;
}
