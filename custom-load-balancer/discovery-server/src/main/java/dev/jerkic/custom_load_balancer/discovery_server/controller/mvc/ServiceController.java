package dev.jerkic.custom_load_balancer.discovery_server.controller.mvc;

import dev.jerkic.custom_load_balancer.discovery_server.service.ServiceManagement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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

  @GetMapping("/service/{serviceId}")
  public String services(Model model, @PathVariable String serviceId) {
    var serviceInstances = this.serviceManagement.getInstacesForService(serviceId);
    var serviceInfo = this.serviceManagement.getServiceInfo(serviceId);

    model.addAttribute("instances", serviceInstances);
    model.addAttribute("serviceInfo", serviceInfo);

    return "service-detail";
  }
}
