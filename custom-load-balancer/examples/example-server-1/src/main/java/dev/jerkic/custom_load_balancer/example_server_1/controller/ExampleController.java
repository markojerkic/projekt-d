package dev.jerkic.custom_load_balancer.example_server_1.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExampleController {

  @GetMapping("/test")
  public ResponseEntity<String> test() {
    return ResponseEntity.ok("Hello from ExampleServer1");
  }
}
