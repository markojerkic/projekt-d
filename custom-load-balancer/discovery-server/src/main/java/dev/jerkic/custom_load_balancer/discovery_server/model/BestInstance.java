package dev.jerkic.custom_load_balancer.discovery_server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.sql.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "best_instance")
@Data
@NoArgsConstructor
public class BestInstance {
  @Id private String entryId;
  private Long serviceId;
  private Date latestTimestamp;

  @OneToOne
  @JoinColumn(name = "entry_id")
  private ServiceInstance serviceInstance;
}
