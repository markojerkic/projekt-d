package dev.jerkic.custom_load_balancer.client.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "discovery-client")
public class ClientProperties {
  private String discoveryServerUrl;
  private String serviceName;
}
