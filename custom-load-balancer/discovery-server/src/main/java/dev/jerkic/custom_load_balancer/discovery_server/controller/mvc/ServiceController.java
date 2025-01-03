package dev.jerkic.custom_load_balancer.discovery_server.controller.mvc;

import dev.jerkic.custom_load_balancer.discovery_server.service.ServiceManagement;
import dev.jerkic.custom_load_balancer.discovery_server.service.ServiceResolverServiceImpl;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
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
  private final ServiceResolverServiceImpl serviceResolverServiceImpl;

  @GetMapping
  public String index(Model model) {
    model.addAttribute("services", serviceManagement.getServices());
    return "index";
  }

  @GetMapping("/service/{serviceId}")
  public String instances(Model model, @PathVariable String serviceId) {
    var serviceInstances = this.serviceResolverServiceImpl.resolveServiceForServiceId(serviceId);
    var serviceInfo = this.serviceManagement.getServiceInfo(serviceId);

    log.info("Service instances: {}", serviceInstances);
    log.info("Service info: {}", serviceInfo);

    model.addAttribute("instances", serviceInstances);
    model.addAttribute("serviceInfo", serviceInfo);

    return "service-detail";
  }

  @HxRequest(boosted = false)
  @GetMapping("/service/{serviceId}")
  public String hxInstances(Model model, @PathVariable String serviceId) {
    var serviceInstances = this.serviceResolverServiceImpl.resolveServiceForServiceId(serviceId);
    var serviceInfo = this.serviceManagement.getServiceInfo(serviceId);

    log.info("Service instances: {}", serviceInstances);
    log.info("Service info: {}", serviceInfo);

    model.addAttribute("instances", serviceInstances);
    model.addAttribute("serviceInfo", serviceInfo);

    return "partials/services :: serviceDetail";
  }
}
