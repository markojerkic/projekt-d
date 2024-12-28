package dev.jerkic.custom_load_balancer.discovery_server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString(exclude = "instances")
@EqualsAndHashCode(exclude = "instances")
public class ServiceModel {
  @Id
  @Column(columnDefinition = "TEXT")
  private UUID id;

  private String serviceName;

  @Builder.Default
  @OneToMany(mappedBy = "serviceModel", fetch = FetchType.LAZY)
  private Set<ServiceInstance> instances = new HashSet<>();
}
