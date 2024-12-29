package dev.jerkic.custom_load_balancer.shared.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceHealthInput {
  @NotNull @NotEmpty private String serviceName;
  @NotNull private boolean isHealthy;
  @NotNull private Long numberOfConnections;
  @NotNull private Instant timestamp;
  @NotNull @NotEmpty private String address;
}
