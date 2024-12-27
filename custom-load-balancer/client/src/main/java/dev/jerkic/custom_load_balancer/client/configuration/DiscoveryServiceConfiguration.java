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
        .isHealthy(this.isHealthy())
        .numberOfConnections(this.getCurrentNumberOfConnections())
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
    try {
      var threadMetrics = this.getThreadMetrics();

      var currentThreadCount = (int) threadMetrics.get("currentThreadCount");
      var currentThreadsBusy = (int) threadMetrics.get("currentThreadsBusy");
      return currentThreadCount - currentThreadsBusy;
    } catch (MalformedObjectNameException | InstanceNotFoundException | ReflectionException e) {
      log.error("Error while getting current number of connections", e);
    }
    return -1;
  }

  /**
   * Check if service is healthy
   *
   * @return true if service is healthy, false otherwise
   */
  private boolean isHealthy() {
    try {
      // Check thread health
      Map<String, Object> threadMetrics = this.getThreadMetrics();
      int busyThreads = (int) threadMetrics.get("currentThreadsBusy");
      int maxThreads = (int) threadMetrics.get("maxThreads");
      double threadUsageRatio = (double) busyThreads / maxThreads;
      if (threadUsageRatio > THREAD_USAGE_THRESHOLD) {
        return false;
      }

      // Check memory health
      Map<String, Object> memoryMetrics = this.getMemoryMetrics();
      long usedMemory = (long) memoryMetrics.get("heapMemoryUsed");
      long maxMemory = (long) memoryMetrics.get("heapMemoryMax");
      double memoryUsageRatio = (double) usedMemory / maxMemory;
      if (memoryUsageRatio > MEMORY_USAGE_THRESHOLD) {
        return false;
      }

      // Additional health checks could be added here

      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Get thread metrics. This method uses Tomcat MBeans to get thread metrics.
   *
   * @return map of thread metrics containing "currentThreadCount", "currentThreadsBusy",
   *     "availableThreads" and "maxThreads"
   */
  private Map<String, Object> getThreadMetrics()
      throws MalformedObjectNameException, InstanceNotFoundException, ReflectionException {
    Map<String, Object> threadMetrics = new HashMap<>();

    ObjectName objectName = new ObjectName("Tomcat:type=ThreadPool,name=\"http-nio-8080\"");
    MBeanServer mBeanServer = Registry.getRegistry(null, null).getMBeanServer();

    AttributeList attributes =
        mBeanServer.getAttributes(
            objectName, new String[] {"currentThreadCount", "currentThreadsBusy", "maxThreads"});

    attributes
        .asList()
        .forEach(attribute -> threadMetrics.put(attribute.getName(), attribute.getValue()));

    // Calculate available threads
    int currentThreadCount = (int) threadMetrics.get("currentThreadCount");
    int busyThreads = (int) threadMetrics.get("currentThreadsBusy");
    threadMetrics.put("availableThreads", currentThreadCount - busyThreads);

    return threadMetrics;
  }

  /**
   * Get memory metrics. This method uses Java Management API to get memory metrics.
   *
   * @return map of memory metrics containing "heapMemoryUsed" and "heapMemoryMax"
   */
  private Map<String, Object> getMemoryMetrics() {
    Map<String, Object> memoryMetrics = new HashMap<>();

    MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    memoryMetrics.put("heapMemoryUsed", memoryBean.getHeapMemoryUsage().getUsed());
    memoryMetrics.put("heapMemoryMax", memoryBean.getHeapMemoryUsage().getMax());

    return memoryMetrics;
  }
}
