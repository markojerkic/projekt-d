package dev.jerkic.custom_load_balancer.client.configuration;

import dev.jerkic.custom_load_balancer.client.properties.ClientProperties;
import dev.jerkic.custom_load_balancer.client.service.ClientHealthService;
import dev.jerkic.custom_load_balancer.shared.model.dto.HealthUpdateInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.RegisterInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.ServiceHealthInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.ServiceInfo;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

  @Value("${server.port}")
  private int serverPort;

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
    try {
      var serviceId = this.clientHealthService.registerService(registerInput);
      log.info("Registered service, id is {}", serviceId);
    } catch (Exception e) {
      log.error("Error registering service. Going to after 10s");

      Executors.newSingleThreadScheduledExecutor()
          .schedule(
              () -> {
                register();
              },
              30,
              TimeUnit.SECONDS);

      this.register();
    }
  }

  // Define cron job for every 1 min
  @Scheduled(fixedRate = 60000)
  public void updateHealth() {
    try {

      var oInstanceId = this.clientHealthService.getInstanceId();
      if (oInstanceId.isEmpty()) {
        throw new IllegalStateException("Service not registered");
      }
      var instanceId = oInstanceId.get();

      var healthStatus =
          HealthUpdateInput.builder()
              .instanceId(instanceId)
              .serviceName(this.clientProperties.getServiceName())
              .health(this.getServiceHealth())
              .build();

      log.info("Updating health: {}", healthStatus);

      this.clientHealthService.updateHealth(healthStatus);
    } catch (Exception e) {
      log.error("Error updating health", e);
      log.warn("Trying to register again after failed health udpate");
      this.register();
    }
  }

  private final ServiceInfo getServiceInfo() {
    return ServiceInfo.builder()
        .serviceName(this.clientProperties.getServiceName())
        .serviceHealthCheckUrl("/health")
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
        .address(this.getCurrentServerAddress())
        .build();
  }

  /**
   * Get address of current server
   *
   * @return String host:port
   */
  private String getCurrentServerAddress() {
    return String.valueOf(this.serverPort);
  }
}
