package dev.jerkic.custom_load_balancer.discovery_server.controller.mvc;

import dev.jerkic.custom_load_balancer.discovery_server.service.ServiceManagement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ServiceController {
  private final ServiceManagement serviceManagement;

  @GetMapping
  public String index(Model model) {
    model.addAttribute("services", serviceManagement.getServices());
    return "index";
  }
}
