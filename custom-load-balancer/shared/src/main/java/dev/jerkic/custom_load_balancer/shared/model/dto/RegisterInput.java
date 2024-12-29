package dev.jerkic.custom_load_balancer.shared.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterInput {
  @NotNull @NotEmpty private ServiceHealthInput serviceHealth;
  @NotNull @NotEmpty private ServiceInfo serviceInfo;
}
