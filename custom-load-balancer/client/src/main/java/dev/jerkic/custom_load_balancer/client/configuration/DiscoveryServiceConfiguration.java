package dev.jerkic.custom_load_balancer.client.configuration;

import dev.jerkic.custom_load_balancer.client.properties.ClientProperties;
import dev.jerkic.custom_load_balancer.client.service.ActiveRequestsService;
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
  private final ActiveRequestsService activeRequestsService;

  @Value("${server.port}")
  private int serverPort;

  @Value("${server.servlet.context-path:/}")
  private String baseHref;

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

    try {
      var instanceId = this.clientHealthService.registerService(registerInput);
      log.info("Registered service, id is {}", instanceId);
    } catch (Exception e) {
      log.error("Error registering service. Going to after 10s");

      Executors.newSingleThreadScheduledExecutor()
          .schedule(
              () -> {
                log.info("Retrying registration");
                register();
              },
              10,
              TimeUnit.SECONDS);
    }
  }

  // Define cron job for every 1 min
  @Scheduled(fixedRate = 60000)
  public void updateHealth() {
    try {

      var oInstanceId = this.clientHealthService.getInstanceId();
      if (oInstanceId.isEmpty()) {
        log.warn("Service not registered");
        return;
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
        .baseHref(this.getBaseHref())
        .build();
  }

  private String getBaseHref() {
    if (this.baseHref.startsWith("/")) {
      return this.baseHref;
    }
    return "/" + this.baseHref;
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
        .numberOfConnections((long) this.activeRequestsService.getActiveRequests())
        .serverPort(String.valueOf(this.serverPort))
        .build();
  }
}
