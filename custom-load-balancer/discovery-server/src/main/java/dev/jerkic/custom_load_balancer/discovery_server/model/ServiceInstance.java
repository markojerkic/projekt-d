package dev.jerkic.custom_load_balancer.discovery_server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
    indexes = {
      @Index(name = "service_instance_instance_id_index", columnList = "instanceId"),
      @Index(
          name = "service_instance_timestamp_desc_health_index",
          columnList = "timestamp DESC, isHealthy")
    })
public class ServiceInstance {
  @Id
  @Column(columnDefinition = "TEXT")
  private UUID entryId;

  private UUID instanceId;
  private boolean isHealthy;
  private String address;
  private Long numberOfConnections;
  private Date timestamp;

  @ManyToOne(fetch = FetchType.LAZY)
  private ServiceModel serviceModel;
}
