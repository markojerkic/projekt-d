package dev.jerkic.custom_load_balancer.discovery_server.model.dto;

public record RegisterInput(
    String serviceName, String serviceBaseUrl, String serviceHealthCheckUrl) {}
