package dev.jerkic.custom_load_balancer.discovery_server.service.http;

import dev.jerkic.custom_load_balancer.discovery_server.exceptions.NoInstanceFoundException;
import dev.jerkic.custom_load_balancer.discovery_server.service.LoadBalancingService;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

@RequiredArgsConstructor
@Slf4j
public class LoadBalancingHttpRequestInterceptor implements ClientHttpRequestInterceptor {

  private final LoadBalancingService loadBalancer;

  @Override
  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

    var bestInstance = this.loadBalancer.findBestInstanceForBaseHref(request.getURI().toString());
    if (bestInstance.isEmpty()) {
      throw new NoInstanceFoundException(
          "No instance found for the given base href " + request.getURI());
    }

    URI uri;
    try {
      var realRequestUri = bestInstance.get().getAddress() + request.getURI().toString();
      log.info("Sending request to {}", realRequestUri);
      uri = new URI(realRequestUri);
    } catch (URISyntaxException e) {
      log.error("Error building real uri", e);
      throw new RuntimeException(e);
    }

    HttpRequest newRequest =
        new HttpRequestImplementation(
            uri,
            request.getURI().toString(),
            request.getHeaders(),
            request.getMethod(),
            request.getAttributes());
    return execution.execute(newRequest, body);
  }
}
