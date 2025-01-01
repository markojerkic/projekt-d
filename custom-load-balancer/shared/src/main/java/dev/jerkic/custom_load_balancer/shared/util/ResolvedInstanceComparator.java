package dev.jerkic.custom_load_balancer.shared.util;

import dev.jerkic.custom_load_balancer.shared.model.UsedResolvedInstance;
import java.util.Comparator;

public class ResolvedInstanceComparator implements Comparator<UsedResolvedInstance> {

  @Override
  public int compare(UsedResolvedInstance o1, UsedResolvedInstance o2) {
    var activeRequestsCompare =
        Long.compare(o1.getInstance().getActiveRequests(), o2.getInstance().getActiveRequests());

    if (activeRequestsCompare != 0) {
      return activeRequestsCompare;
    }

    return o1.getUsedAt().compareTo(o2.getUsedAt());
  }
}
