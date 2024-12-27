package dev.jerkic.custom_load_balancer.discovery_server.util;

import dev.jerkic.custom_load_balancer.discovery_server.model.dto.ServiceHealthInput;
import java.util.Comparator;

public class ServiceHealthComparator implements Comparator<ServiceHealthInput> {

  private static final ServiceHealthComparator INSTANCE = new ServiceHealthComparator();

  private ServiceHealthComparator() {}

  public static ServiceHealthComparator getInstance() {
    return INSTANCE;
  }

  @Override
  public int compare(ServiceHealthInput o1, ServiceHealthInput o2) {
    return o1.getTimestamp().compareTo(o2.getTimestamp());
  }
}
