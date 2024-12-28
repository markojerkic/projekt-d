package dev.jerkic.custom_load_balancer.shared.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceInfo {
  private String serviceName;
  private String serviceHealthCheckUrl;
}
