package dev.jerkic.custom_load_balancer.discovery_server.model.projection;

import lombok.Data;

@Data
public class ServiceModelLazyProjection {
  private String id;
  private String serviceName;
}
