package dev.jerkic.custom_load_balancer.client.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "discovery.client")
@Configuration
public class ClientProperties {
  private final String discoveryServerUrl;
  private final String serviceName;
}
