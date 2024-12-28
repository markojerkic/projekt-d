package dev.jerkic.custom_load_balancer.client.service;

import dev.jerkic.custom_load_balancer.client.properties.ClientProperties;
import dev.jerkic.custom_load_balancer.shared.model.dto.HealthUpdateInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.RegisterInput;
import dev.jerkic.custom_load_balancer.shared.service.ServiceHealthService;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientHealthService implements ServiceHealthService {
  private final ClientProperties clientProperties;
  private final RestTemplate restTemplate = new RestTemplate();
  private final AtomicReference<Optional<String>> instanceId =
      new AtomicReference<>(Optional.empty());

  @Override
  public String registerService(RegisterInput registerInput) {
    var instanceId =
        this.restTemplate.postForObject(
            this.clientProperties.getDiscoveryServerUrl() + "/register",
            registerInput,
            String.class);

    this.instanceId.set(Optional.ofNullable(instanceId));
    return instanceId;
  }

  @Override
  public void updateHealth(HealthUpdateInput healthUpdateInput) {
    this.restTemplate.postForObject(
        this.clientProperties.getDiscoveryServerUrl() + "/health", healthUpdateInput, Void.class);
  }

  public Optional<String> getInstanceId() {
    return Optional.ofNullable(this.instanceId.get().get());
  }
}
