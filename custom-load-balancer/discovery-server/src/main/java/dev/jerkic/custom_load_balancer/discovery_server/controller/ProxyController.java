package dev.jerkic.custom_load_balancer.discovery_server.controller;

import dev.jerkic.custom_load_balancer.discovery_server.config.ProxyRestTemplate;
import dev.jerkic.custom_load_balancer.discovery_server.util.RequestEntityConverter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
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

  @RequestMapping("/**")
  public ResponseEntity<?> proxy(HttpServletRequest request) throws IOException {
    var requestedPath = request.getRequestURI();

    log.debug("Requested path: {}", requestedPath);

    var requestEntity = RequestEntityConverter.fromHttpServletRequest(request);
    return this.restTemplate.exchange(requestEntity, String.class);
  }
}
