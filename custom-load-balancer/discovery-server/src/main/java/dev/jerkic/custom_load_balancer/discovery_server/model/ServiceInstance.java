package dev.jerkic.custom_load_balancer.discovery_server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
          columnList = "instanceRecordedAt DESC, isHealthy")
    })
@ToString(exclude = {"serviceModel"})
@EqualsAndHashCode(exclude = {"serviceModel"})
public class ServiceInstance {
  @Id private String entryId;
  private String instanceId;
  private boolean isHealthy;
  private String address;
  private Long activeHttpRequests;
  private Date instanceRecordedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  private ServiceModel serviceModel;
}
