package dev.jerkic.custom_load_balancer.discovery_server.controller;

import dev.jerkic.custom_load_balancer.discovery_server.service.ServiceManagement;
import dev.jerkic.custom_load_balancer.shared.model.dto.RegisterInput;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/register")
public class RegisterController {
  private final ServiceManagement serviceManagement;

  @PostMapping
  public String registerService(@RequestBody @Validated RegisterInput registerInput) {
    return this.serviceManagement.registerService(registerInput);
  }
}
