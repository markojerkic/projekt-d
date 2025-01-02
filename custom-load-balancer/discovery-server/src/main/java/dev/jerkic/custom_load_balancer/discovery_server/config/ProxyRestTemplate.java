package dev.jerkic.custom_load_balancer.discovery_server.config;

import dev.jerkic.custom_load_balancer.discovery_server.service.http.LoadBalancingHttpRequestInterceptor;
import dev.jerkic.custom_load_balancer.shared.service.ServiceResolverService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ProxyRestTemplate extends RestTemplate {

  @Autowired
  public ProxyRestTemplate(ServiceResolverService serviceResolverService) {
    super();
    this.setInterceptors(List.of(new LoadBalancingHttpRequestInterceptor(serviceResolverService)));
  }
}
