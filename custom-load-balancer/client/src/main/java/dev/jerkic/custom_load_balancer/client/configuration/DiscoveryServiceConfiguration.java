package dev.jerkic.custom_load_balancer.client.configuration;

import dev.jerkic.custom_load_balancer.client.properties.ClientProperties;
import dev.jerkic.custom_load_balancer.client.service.ClientHealthService;
import dev.jerkic.custom_load_balancer.shared.model.dto.HealthUpdateInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.RegisterInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.ServiceHealthInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.ServiceInfo;
import jakarta.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.core.StandardService;
import org.apache.tomcat.util.modeler.Registry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
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

  private final ClientHealthService clientHealthService;
  private final ClientProperties clientProperties;
  private final StandardServer standardServer = new StandardServer();

  private final ServletWebServerApplicationContext server;

  /** Register on every startup of server */
  @PostConstruct
  public void register() {
    var registerInput = RegisterInput.builder()
      .serviceInfo(this.getServiceInfo())
      .serviceHealth(this.getServiceHealth())
      .build();

    log.info("Registering server with service discovery with input: {}", registerInput);
    this.clientHealthService.registerService(registerInput    );
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
    return String.valueOf( this.server.getWebServer().getPort());
  }

  /**
   * Return number of curent open HTTP connections
   *
   * @return number of open connections or -1 if error occurred
   */
  private long getCurrentNumberOfConnections() {
      StandardService service = (StandardService) standardServer.findService("Catalina");
        Connector[] connectors = service.findConnectors();

        int activeConnections = 0;
        for (Connector connector : connectors) {
            // Access the ProtocolHandler and retrieve connection stats
            if (connector.getProtocolHandler() != null) {
                activeConnections += ((org.apache.coyote.AbstractProtocol<?>) connector.getProtocolHandler()).getConnectionCount();
            }
        }
        return activeConnections;

  }

  /**
   * Check if service is healthy
   *
   * @return true if service is healthy, false otherwise
   */
  private boolean isHealthy() {
    return this.isThreadHealthy() && this.isMemoryHealthy();
  }

  private boolean isMemoryHealthy() {
    try {
        // Using ManagementFactory to get memory usage information
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        long usedMemory = heapMemoryUsage.getUsed();
        long maxMemory = heapMemoryUsage.getMax();
        double memoryUsageRatio = (double) usedMemory / maxMemory;

        return memoryUsageRatio <= MEMORY_USAGE_THRESHOLD;
    } catch (Exception e) {
      log.error("Error getting memory health", e);
        return false;
    }
  }

  private boolean isThreadHealthy() {
       try {
        // Using ManagementFactory to get thread usage information
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        // Get the current number of busy threads
        int busyThreads = threadMXBean.getThreadCount();  // Total number of threads in the JVM

        // Get the maximum number of threads allowed by the JVM
        int maxThreads = Runtime.getRuntime().availableProcessors() * 2;  // Example assumption

        // Calculate thread usage ratio
        double threadUsageRatio = (double) busyThreads / maxThreads;

        return threadUsageRatio <= THREAD_USAGE_THRESHOLD;
    } catch (Exception e) {
        // Log the exception if necessary
        return false;
    }
  }


}
