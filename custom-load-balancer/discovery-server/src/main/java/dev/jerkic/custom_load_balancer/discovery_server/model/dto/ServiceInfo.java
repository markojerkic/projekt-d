package dev.jerkic.custom_load_balancer.discovery_server.model.dto;

import lombok.Data;

@Data
public class ServiceInfo {
  private final String serviceName;
  private final String serviceAddress;
  private final String serviceHealthCheckUrl;
}
