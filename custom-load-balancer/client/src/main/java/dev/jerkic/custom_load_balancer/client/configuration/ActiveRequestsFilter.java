package dev.jerkic.custom_load_balancer.client.configuration;

import dev.jerkic.custom_load_balancer.client.service.ActiveRequestsService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActiveRequestsFilter implements Filter {
  private final ActiveRequestsService activeRequestsService;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    this.activeRequestsService.incrementActiveRequests();

    try {
      chain.doFilter(request, response);
      this.activeRequestsService.decrementActiveRequests();
    } catch (Exception e) {

      this.activeRequestsService.decrementActiveRequests();
      throw e;
    }
  }
}
