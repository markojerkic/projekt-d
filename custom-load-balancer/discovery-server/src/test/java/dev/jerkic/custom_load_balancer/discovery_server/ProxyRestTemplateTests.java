package dev.jerkic.custom_load_balancer.discovery_server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.jerkic.custom_load_balancer.discovery_server.config.ProxyRestTemplate;
import dev.jerkic.custom_load_balancer.discovery_server.service.ServiceManagement;
import dev.jerkic.custom_load_balancer.shared.model.dto.RegisterInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.ServiceHealthInput;
import dev.jerkic.custom_load_balancer.shared.model.dto.ServiceInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class ProxyRestTemplateTests {
  @Autowired private ServiceManagement serviceManagement;
  @Autowired private ProxyRestTemplate proxyRestTemplate;

  @Autowired private HttpServletRequest httpServletRequest;
  private MockRestServiceServer mockServer;

  @TestConfiguration
  static class TestConfig {
    @Bean
    @Primary
    public HttpServletRequest mockRequest() {
      var request = Mockito.mock(HttpServletRequest.class);

      return request;
    }
  }

  @BeforeEach
  void setUp() {
    this.mockServer = MockRestServiceServer.bindTo(this.proxyRestTemplate).build();
  }

  @Test
  public void testRequest() {
    var randomResponse = String.valueOf(Math.random() * 1000);
    this.mockServer
        .expect(MockRestRequestMatchers.requestTo("http://192.0.0.1:8090/test/something/2"))
        .andRespond(MockRestResponseCreators.withSuccess(randomResponse, MediaType.TEXT_PLAIN));

    // Mock HttpServletRequest behavior
    Mockito.when(httpServletRequest.getRemoteHost()).thenReturn("192.0.0.1");
    Mockito.when(httpServletRequest.getScheme()).thenReturn("http");

    // Register a service
    var serviceName = "test-service";
    var baseHref = "/test";
    var registerInput =
        RegisterInput.builder()
            .serviceInfo(this.getServiceInfo(serviceName, baseHref))
            .serviceHealth(this.getServiceHealth("8090", true, serviceName))
            .build();
    registerInput.getServiceHealth().setTimestamp(Instant.now().minusSeconds(100));

    this.serviceManagement.registerService(registerInput);

    // Verify the request
    var response = this.proxyRestTemplate.getForEntity("/test/something/2", String.class);

    assertEquals(200, response.getStatusCode().value());
    assertEquals(randomResponse, response.getBody());
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
