package dev.jerkic.custom_load_balancer.shared.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterInput {
  @NotNull private ServiceHealthInput serviceHealth;
  @NotNull private ServiceInfo serviceInfo;
}
