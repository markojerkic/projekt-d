package dev.jerkic.custom_load_balancer.discovery_server.service;

import dev.jerkic.custom_load_balancer.discovery_server.repository.ServiceInstanceRepository;
import dev.jerkic.custom_load_balancer.shared.model.dto.ResolvedInstance;
import dev.jerkic.custom_load_balancer.shared.service.ServiceResolverService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServiceResolverServiceImpl implements ServiceResolverService {
  private final ServiceInstanceRepository serviceInstanceRepository;
  private final JdbcTemplate jdbcTemplate;

  @Override
  public List<ResolvedInstance> resolveService(String serviceName) {
    return Collections.emptyList();
  }

  @Override
  public Optional<ResolvedInstance> resolveBestInstance(String serviceName) {
    return Optional.empty();
  }
}
