package dev.jerkic.custom_load_balancer.client.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@ConfigurationProperties(prefix = "discovery.client")
@Configuration
@AllArgsConstructor
public class ClientProperties {
  private String discoveryServerUrl;
  private String serviceName;
}
