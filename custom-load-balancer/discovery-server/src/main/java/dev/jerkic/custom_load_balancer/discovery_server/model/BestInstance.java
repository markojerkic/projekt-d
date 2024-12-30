package dev.jerkic.custom_load_balancer.discovery_server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.sql.Date;
import lombok.Getter;
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
@Getter
@NoArgsConstructor
public class BestInstance {
  @Id
  @Column(name = "entry_id")
  private String entryId;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "entry_id", insertable = false, updatable = false)
  private ServiceInstance serviceInstance;

  @Column(name = "service_id")
  private String serviceId;

  @Column(name = "latest_timestamp")
  private Date latestTimestamp;
}
