package dev.jerkic.custom_load_balancer.discovery_server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
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
@ToString(exclude = "instances")
@EqualsAndHashCode(exclude = "instances")
@Entity
@Table(
    indexes = {
      @Index(name = "service_name_base_href", columnList = "serviceName, baseHref", unique = true)
    })
public class ServiceModel {
  @Id private String id;

  @Column(unique = true, nullable = false)
  private String serviceName;

  @Column private String baseHref;

  @Builder.Default
  @OneToMany(mappedBy = "serviceModel", fetch = FetchType.LAZY)
  private Set<ServiceInstance> instances = new HashSet<>();
}
