package dev.jerkic.custom_load_balancer.client.configuration;

import dev.jerkic.custom_load_balancer.client.properties.ClientProperties;
import dev.jerkic.custom_load_balancer.client.service.ClientHealthService;
import dev.jerkic.custom_load_balancer.shared.model.dto.HealthUpdateInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.RegisterInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.ServiceHealthInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.ServiceInfo;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
@EnableScheduling
@EnableConfigurationProperties(ClientProperties.class)
@Slf4j
public class DiscoveryServiceConfiguration {
  // Threshold for threads. If less than 85% of threads are available, service is considered
  // unhealthy
  private static final double THREAD_USAGE_THRESHOLD = 0.85;
  private static final double MEMORY_USAGE_THRESHOLD = 0.90;

  private final ServerProperties serverProperties;

  private final ClientHealthService clientHealthService;
  private final ClientProperties clientProperties;

  /** Register on every startup of server */
  @PostConstruct
  public void register() {
    var registerInput =
        RegisterInput.builder()
            .serviceInfo(this.getServiceInfo())
            .serviceHealth(this.getServiceHealth())
            .build();

    log.info("Registering server with service discovery with input: {}", registerInput);
    var serviceId = this.clientHealthService.registerService(registerInput);
    log.info("Registered service, id is {}", serviceId);
  }

  // Define cron job for every 1 min
  @Scheduled(fixedRate = 60000)
  public void updateHealth() {

    var oServiceId = this.clientHealthService.getServiceId();
    if (oServiceId.isEmpty()) {
      throw new IllegalStateException("Service not registered");
    }
    var serviceId = oServiceId.get();

    var healthStatus =
        HealthUpdateInput.builder().serviceId(serviceId).health(this.getServiceHealth()).build();

    log.info("Updating health: {}", healthStatus);

    this.clientHealthService.updateHealth(healthStatus);
  }

  private final ServiceInfo getServiceInfo() {
    return ServiceInfo.builder()
        .serviceName(this.clientProperties.getServiceName())
        .serviceHealthCheckUrl("/health")
        .serviceAddress(this.getCurrentServerAddress())
        .build();
  }

  /**
   * Get service health. This method checks if service is healthy and returns ServiceHealthInput
   * object
   *
   * @return ServiceHealthInput object
   */
  private ServiceHealthInput getServiceHealth() {
    return ServiceHealthInput.builder()
        .serviceName(this.clientProperties.getServiceName())
        .timestamp(Instant.now())
        .isHealthy(true)
        .numberOfConnections(0l)
        .build();
  }

  /**
   * Get address of current server
   *
   * @return String host:port
   */
  private String getCurrentServerAddress() {
    return String.valueOf(this.serverProperties.getPort());
  }
}
