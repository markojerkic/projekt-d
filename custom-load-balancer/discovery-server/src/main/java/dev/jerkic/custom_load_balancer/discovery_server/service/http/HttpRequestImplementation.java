package dev.jerkic.custom_load_balancer.discovery_server.service.http;

import java.net.URI;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;

@RequiredArgsConstructor
class HttpRequestImplementation implements HttpRequest {
  private final URI uri;
  private final String originalUrl;
  private final HttpHeaders originalHeaders;
  private final HttpMethod originalMethod;
  private final Map<String, Object> originalAttributes;

  @Override
  public HttpHeaders getHeaders() {
    this.originalHeaders.add("X-Forwarded-Host", this.originalUrl);
    System.out.println("TU sam");
    System.out.println("TU sam");
    System.out.println("TU sam");
    System.out.println("TU sam");
    return this.originalHeaders;
  }

  @Override
  public HttpMethod getMethod() {
    return this.originalMethod;
  }

  @Override
  public URI getURI() {
    return this.uri;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return this.originalAttributes;
  }
}
