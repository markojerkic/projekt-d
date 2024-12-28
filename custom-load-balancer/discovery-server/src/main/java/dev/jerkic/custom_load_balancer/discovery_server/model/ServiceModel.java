package dev.jerkic.custom_load_balancer.discovery_server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceModel {
  @Id private UUID serviceId;
  private String serviceName;

  @Builder.Default
  @OneToMany(mappedBy = "serviceModel", fetch = FetchType.LAZY)
  private Set<ServiceInstance> instances = new HashSet<>();
}
