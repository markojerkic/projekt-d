package dev.jerkic.custom_load_balancer.client.service;

import dev.jerkic.custom_load_balancer.client.properties.ClientProperties;
import dev.jerkic.custom_load_balancer.shared.model.dto.HealthUpdateInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.RegisterInput;
import dev.jerkic.custom_load_balancer.shared.service.ServiceHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(ClientProperties.class)
@RequiredArgsConstructor
public class ClientHealthService implements ServiceHealthService {
  private final ClientProperties clientProperties;

  @Override
  public String registerService(RegisterInput registerInput) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateHealth(HealthUpdateInput healthUpdateInput) {
    // TODO Auto-generated method stub

  }
}
