package dev.jerkic.custom_load_balancer.shared.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResolvedInstance {
  @NotNull @NotEmpty private String instanceId;
  @NotNull @NotEmpty private String address;

  @NotNull
  @Min(0)
  private Long activeRequests;

  @NotNull private Boolean isHealthy;
}
