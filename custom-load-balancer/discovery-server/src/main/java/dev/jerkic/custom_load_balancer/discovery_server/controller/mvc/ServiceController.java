package dev.jerkic.custom_load_balancer.discovery_server.controller.mvc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ServiceController {
  @GetMapping
  public String index() {
    return "index";
  }
}
