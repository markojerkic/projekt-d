package dev.jerkic.custom_load_balancer.discovery_server.controller;

import dev.jerkic.custom_load_balancer.discovery_server.config.ProxyRestTemplate;
import dev.jerkic.custom_load_balancer.discovery_server.service.LoadBalancingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProxyController {
  private final ProxyRestTemplate restTemplate;
  private final LoadBalancingService loadBalancingService;

  @RequestMapping("/**")
  public ResponseEntity<?> proxy(HttpServletRequest request) {
    var requestedPath = request.getRequestURI();

    log.info("Requested path: {}", requestedPath);
    log.info("Request: {}", request.getRequestURI());

    return ResponseEntity.ok("Nema još ništa");
  }
}