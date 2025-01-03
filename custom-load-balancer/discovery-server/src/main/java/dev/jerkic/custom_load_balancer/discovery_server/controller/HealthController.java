package dev.jerkic.custom_load_balancer.discovery_server.controller;

import dev.jerkic.custom_load_balancer.discovery_server.service.ServiceManagement;
import dev.jerkic.custom_load_balancer.shared.model.dto.HealthUpdateInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/health")
public class HealthController {
  private final ServiceManagement serviceManagement;

  @PostMapping
  public void updateHealth(@RequestBody HealthUpdateInput healthUpdateInput) {
    log.info("Updating health: {}", healthUpdateInput);

    this.serviceManagement.updateHealth(healthUpdateInput);
  }
}
