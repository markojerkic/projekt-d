package dev.jerkic.custom_load_balancer.discovery_server.controller;

import dev.jerkic.custom_load_balancer.discovery_server.model.dto.ServiceInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/register")
public class RegisterController {
  @PostMapping
  public void register(@RequestBody ServiceInfo registerInput) {}
}
