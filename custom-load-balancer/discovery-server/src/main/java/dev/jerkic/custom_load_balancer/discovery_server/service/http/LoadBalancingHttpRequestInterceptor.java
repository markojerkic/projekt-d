package dev.jerkic.custom_load_balancer.discovery_server.service.http;

import dev.jerkic.custom_load_balancer.discovery_server.exceptions.NoInstanceFoundException;
import dev.jerkic.custom_load_balancer.shared.service.LoadBalancingService;
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
  private final LoadBalancingService loadBalancingService;

  @Override
  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

    var bestInstance =
        this.loadBalancingService.getBestInstanceForBaseHref(request.getURI().toString());

    if (bestInstance.isEmpty()) {
      throw new NoInstanceFoundException(
          "No instance found for the given base href " + request.getURI());
    }

    var uri = this.getProxiedUriFromOriginal(bestInstance.get().uri(), request);

    HttpRequest newRequest =
        new HttpRequestImplementation(
            uri,
            request.getURI().toString(),
            request.getHeaders(),
            request.getMethod(),
            request.getAttributes());
    var result = execution.execute(newRequest, body);

    var responseHeaders = result.getHeaders();
    responseHeaders.add("X-Load-balanded", "true");
    bestInstance.ifPresent(
        instance -> {
          responseHeaders.add("X-LB-instance", instance.instanceId());
        });

    return result;
  }

  private URI getProxiedUriFromOriginal(String bestInstanceUri, HttpRequest request) {
    try {
      var query = request.getURI().getQuery() == null ? "" : "?" + request.getURI().getQuery();

      var realRequestUri = bestInstanceUri + request.getURI().getPath() + query;
      log.info("Sending request to {}", realRequestUri);
      return new URI(realRequestUri);
    } catch (URISyntaxException e) {
      log.error("Error building real uri", e);
      throw new RuntimeException(e);
    }
  }
}
