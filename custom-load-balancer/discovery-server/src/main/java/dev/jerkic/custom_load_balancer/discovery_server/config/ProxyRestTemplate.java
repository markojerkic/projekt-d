package dev.jerkic.custom_load_balancer.discovery_server.config;

import dev.jerkic.custom_load_balancer.discovery_server.service.LoadBalancingService;
import dev.jerkic.custom_load_balancer.discovery_server.service.http.LoadBalancingHttpRequestInterceptor;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ProxyRestTemplate extends RestTemplate {

  @Autowired
  public ProxyRestTemplate(LoadBalancingService loadBalancingService) {
    super();
    this.setInterceptors(List.of(new LoadBalancingHttpRequestInterceptor(loadBalancingService)));
  }
}
