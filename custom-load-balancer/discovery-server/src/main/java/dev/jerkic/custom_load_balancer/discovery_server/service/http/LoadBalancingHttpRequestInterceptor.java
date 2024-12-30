package dev.jerkic.custom_load_balancer.discovery_server.service.http;

import dev.jerkic.custom_load_balancer.discovery_server.exceptions.NoInstanceFoundException;
import dev.jerkic.custom_load_balancer.discovery_server.service.LoadBalancingService;
import java.io.IOException;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

@RequiredArgsConstructor
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

    URI uri = loadBalancer.getUri(request.getURI());

    HttpRequest newRequest =
        new HttpRequest() {
          @Override
          public HttpMethod getMethod() {
            return request.getMethod();
          }

          @Override
          public URI getURI() {
            return uri;
          }

          @Override
          public HttpHeaders getHeaders() {
            return request.getHeaders();
          }
        };
    return execution.execute(newRequest, body);
  }
}
