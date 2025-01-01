package dev.jerkic.custom_load_balancer.shared.service;

import dev.jerkic.custom_load_balancer.shared.model.dto.ResolvedInstance;
import java.util.List;
import java.util.Optional;

public interface ServiceResolverService {
  List<ResolvedInstance> resolveService(String serviceName);

  List<ResolvedInstance> resolveForBaseHref(String baseHref);

  Optional<ResolvedInstance> resolveBestInstance(String serviceName);
}
