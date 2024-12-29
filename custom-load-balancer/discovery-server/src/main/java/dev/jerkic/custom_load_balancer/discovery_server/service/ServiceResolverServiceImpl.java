package dev.jerkic.custom_load_balancer.discovery_server.service;

import dev.jerkic.custom_load_balancer.discovery_server.model.ServiceInstance;
import dev.jerkic.custom_load_balancer.discovery_server.repository.ServiceInstanceRepository;
import dev.jerkic.custom_load_balancer.shared.model.dto.ResolvedInstance;
import dev.jerkic.custom_load_balancer.shared.service.ServiceResolverService;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ServiceResolverServiceImpl implements ServiceResolverService {
  private ServiceInstanceRepository serviceInstanceRepository;

  @Override
  public List<ResolvedInstance> resolveService(String serviceName) {
    return this.serviceInstanceRepository.findLatestForServiceId(serviceName).stream()
        .map(ServiceInstance::toResolvedInstance)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<ResolvedInstance> resolveBestInstance(String serviceName) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'resolveBestInstance'");
  }
}
