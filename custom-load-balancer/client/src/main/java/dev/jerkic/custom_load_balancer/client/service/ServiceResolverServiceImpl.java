package dev.jerkic.custom_load_balancer.client.service;

import dev.jerkic.custom_load_balancer.shared.model.dto.ResolvedInstance;
import dev.jerkic.custom_load_balancer.shared.service.ServiceResolverService;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ServiceResolverServiceImpl implements ServiceResolverService {

  @Override
  public List<ResolvedInstance> resolveService(String serviceName) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'resolveService'");
  }

  @Override
  public List<ResolvedInstance> resolveForBaseHref(String baseHref) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'resolveForBaseHref'");
  }

  @Override
  public Optional<ResolvedInstance> resolveBestInstance(String serviceName) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'resolveBestInstance'");
  }

  @Override
  public Optional<ResolvedInstance> resolveBestInstanceForBaseHref(String requestedUri) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException(
        "Unimplemented method 'resolveBestInstanceForBaseHref'");
  }
}
