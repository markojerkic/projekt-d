package dev.jerkic.custom_load_balancer.shared.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceInfo {
  @NotBlank private String serviceName;
  private String baseHref;
}
