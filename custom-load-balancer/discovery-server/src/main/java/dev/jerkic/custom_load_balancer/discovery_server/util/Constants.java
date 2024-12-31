package dev.jerkic.custom_load_balancer.discovery_server.util;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

public class Constants {
  public static Sort SORT_INSTANCE =
      Sort.by(Order.desc("latestTimestamp"), Order.asc("serviceInstance.activeHttpRequests"));
}
