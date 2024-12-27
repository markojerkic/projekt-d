package dev.jerkic.custom_load_balancer.client.configuration;

import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TcpConnectionCounter implements TomcatConnectorCustomizer {
  private final AtomicLong connectinoCount = new AtomicLong(0);

  @Override
  public void customize(Connector connector) {
    connector.addLifecycleListener(
        event -> {
          log.info("Tomcat connection event: {}", event);
        });
  }
}
