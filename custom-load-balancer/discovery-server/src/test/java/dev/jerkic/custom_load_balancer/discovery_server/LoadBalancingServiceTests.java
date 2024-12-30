package dev.jerkic.custom_load_balancer.discovery_server;


import dev.jerkic.custom_load_balancer.discovery_server.repository.ServiceInstanceRepository;
import dev.jerkic.custom_load_balancer.discovery_server.service.LoadBalancingService;
import dev.jerkic.custom_load_balancer.discovery_server.service.ServiceManagement;
import dev.jerkic.custom_load_balancer.shared.model.dto.ServiceHealthInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.ServiceInfo;
import jakarta.transaction.Transactional;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class LoadBalancingServiceTests {
  @Autowired private ServiceInstanceRepository serviceInstanceRepository;
  @Autowired private ServiceManagement serviceManagement;
  @Autowired private LoadBalancingService loadBalancingService;

  @Test
  public void testRegisterService() {
    // FIXME: proper request mocking
    //
    // var serviceName = "test-service";
    // var baseHref = "/test";
    // var registerInput =
    //    RegisterInput.builder()
    //        .serviceInfo(this.getServiceInfo(serviceName, baseHref))
    //        .serviceHealth(this.getServiceHealth("8090", true, serviceName))
    //        .build();
    // registerInput.getServiceHealth().setTimestamp(Instant.now().minusSeconds(100));
    //
    // this.serviceManagement.registerService(registerInput);
    //
    // var mockHttpRequest = new MockHttpServletRequest("GET", "/test/something/2");
    //
    // var response = this.loadBalancingService.proxyRequest(mockHttpRequest);
    // assertEquals(200, response.getStatusCode().value());
    // assertEquals("Nema još ništa", response.getBody());
    // assertEquals(1, this.serviceInstanceRepository.count());
    // assertEquals(
    //    1,
    //    this.serviceInstanceRepository.findAll().stream()
    //        .findFirst()
    //        .get()
    //        .getActiveHttpRequests());
  }

  private ServiceHealthInput getServiceHealth(String port, boolean isHealthy, String serviceName) {
    return ServiceHealthInput.builder()
        .serverPort(port)
        .isHealthy(isHealthy)
        .serviceName(serviceName)
        .numberOfConnections(0l)
        .timestamp(Instant.now())
        .build();
  }

  private ServiceInfo getServiceInfo(String serviceName, String baseHref) {
    return ServiceInfo.builder().serviceName(serviceName).baseHref(baseHref).build();
  }
}
