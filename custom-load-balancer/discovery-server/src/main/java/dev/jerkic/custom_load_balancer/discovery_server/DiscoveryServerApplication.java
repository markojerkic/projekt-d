package dev.jerkic.custom_load_balancer.discovery_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DiscoveryServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(DiscoveryServerApplication.class, args);
  }
}
