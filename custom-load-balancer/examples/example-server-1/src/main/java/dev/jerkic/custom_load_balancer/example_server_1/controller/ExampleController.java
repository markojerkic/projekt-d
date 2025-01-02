package dev.jerkic.custom_load_balancer.example_server_1.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExampleController {

  @Value("${spring.application.name}")
  private String applicationName;

  @GetMapping("/test")
  public ResponseEntity<String> test() {
    return ResponseEntity.ok("Hello from fast api of " + applicationName);
  }

  @GetMapping("/slow-request")
  public ResponseEntity<String> testSlow() {
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    return ResponseEntity.ok("Hello from slow api of " + applicationName);
  }
}
