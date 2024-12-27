package dev.jerkic.custom_load_balancer.discovery_server.model;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("service")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceModel {
  @Id private String id;
  private String serviceName;
  // Collection of service instances
  @Builder.Default @Reference private Set<ServiceInstance> instances = new HashSet<>();
}
