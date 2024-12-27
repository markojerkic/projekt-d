package dev.jerkic.custom_load_balancer.shared;

public interface ServiceHealthService {

  /**
   * Registers a service with the discovery server.
   *
   * @param registerInput The service to register.
   * @return The id of the registered service. Used to update the health of the service.
   */
  public String registerService(RegisterInput registerInput) {}

  /**
   * Updates the health of a service.
   *
   * @param healthUpdateInput The health update input.
   */
  public void updateHealth(HealthUpdateInput healthUpdateInput) {}
}
