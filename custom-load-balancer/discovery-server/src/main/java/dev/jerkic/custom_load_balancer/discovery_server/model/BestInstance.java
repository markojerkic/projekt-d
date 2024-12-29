package dev.jerkic.custom_load_balancer.discovery_server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.sql.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;

@Entity
@Immutable
@Subselect(
    """
    SELECT
        s.entry_id,
        s.service_model_id as service_id,
        max(s.instance_recorded_at) as latest_timestamp
    FROM
        service_instance s
    WHERE
        s.is_healthy = 1
        AND (strftime('%s', 'now') * 1000 - s.instance_recorded_at) <= (3*60*1000)
    GROUP BY
        s.instance_id
""")
@Synchronize({"service_instance"})
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
