package dev.jerkic.custom_load_balancer.example_server_1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExampleController {

  @GetMapping
  public String test() {
    return "Hello from example server 1";
  }
}
