package dev.jerkic.custom_load_balancer.shared.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.sql.Date;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResolvedInstance {
  @NotNull @NotEmpty private String instanceId;
  @NotNull @NotEmpty private String address;
  @NotNull @NotEmpty private String baseBref;

  @NotNull
  @Min(0)
  @Builder.Default
  private Long activeRequests = 0l;

  @NotNull private Boolean isHealthy;
  @NotNull private Date recordedAt;
}
