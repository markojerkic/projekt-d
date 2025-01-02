package dev.jerkic.custom_load_balancer.example_server_1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    scanBasePackages = {
      "dev.jerkic.custom_load_balancer",
    })
public class ExampleServer1Application {

  public static void main(String[] args) {
    SpringApplication.run(ExampleServer1Application.class, args);
  }
}
