package dev.jerkic.custom_load_balancer.discovery_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

  @Bean("proxyRestTemplate")
  public RestTemplate proxyRestTemplate() {
    return new RestTemplate();
  }
}
