package dev.jerkic.custom_load_balancer.discovery_server.util;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;

public class RequestEntityConverter {

  public static RequestEntity<?> fromHttpServletRequest(HttpServletRequest request)
      throws IOException {
    // Extract HTTP method
    HttpMethod method = HttpMethod.valueOf(request.getMethod());

    // Extract URI
    URI uri =
        URI.create(
            request.getRequestURL().toString()
                + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));

    // Extract headers
    HttpHeaders headers = new HttpHeaders();
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      Enumeration<String> headerValues = request.getHeaders(headerName);
      while (headerValues.hasMoreElements()) {
        headers.add(headerName, headerValues.nextElement());
      }
    }

    // Extract body
    StringBuilder bodyBuilder = new StringBuilder();
    try (BufferedReader reader = request.getReader()) {
      String line;
      while ((line = reader.readLine()) != null) {
        bodyBuilder.append(line);
      }
    }
    String body = bodyBuilder.toString();

    // Build RequestEntity
    return new RequestEntity<>(body.isEmpty() ? null : body, headers, method, uri);
  }
}
