package dev.jerkic.custom_load_balancer.client.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
@EnableScheduling
public class DiscoveryServiceConfiguration {

  // Define cron job for every 1 min
  @Scheduled(fixedRate = 60000)
  public void register() {
    // Register client to discovery server
    // Use clientProperties.getDiscoveryServerUrl() to get the discovery server URL
    // Use clientProperties.getServiceName() to get the service name
  }
}
