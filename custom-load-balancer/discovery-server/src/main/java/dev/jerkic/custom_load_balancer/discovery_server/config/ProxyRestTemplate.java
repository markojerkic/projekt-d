package dev.jerkic.custom_load_balancer.discovery_server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ProxyRestTemplate extends RestTemplate {
  @Autowired
  public ProxyRestTemplate() {
    super();
    this.setInterceptors()
  }
}
