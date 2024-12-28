package dev.jerkic.custom_load_balancer.discovery_server.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash(value = "service_instance", timeToLive = 5 * 60)
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceInstance {
  @Id private String entryId;
  @Indexed private String instanceId;
  @Indexed private String serviceId;
  @Indexed private boolean isHealthy;
  private String address;
  private Long numberOfConnections;
  private Instant timestamp;
}
