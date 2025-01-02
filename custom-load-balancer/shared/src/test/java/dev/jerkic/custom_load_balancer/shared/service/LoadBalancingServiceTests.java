package dev.jerkic.custom_load_balancer.shared.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import dev.jerkic.custom_load_balancer.shared.model.dto.ResolvedInstance;
import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class LoadBalancingServiceTests {

  private final ServiceResolverService serviceResolverService =
      Mockito.mock(ServiceResolverService.class);

  @AfterEach
  public void tearDown() {
    Mockito.reset(serviceResolverService);
  }

  @Test
  public void testResolveBestServiceForBaseHref() {
    var loadBalancer = new LoadBalancingService(this.serviceResolverService);

    var baseHref = "/test-app";
    var requestUri = "/test-app/marko/3";

    var instance =
        ResolvedInstance.builder()
            .instanceId(UUID.randomUUID().toString())
            .address("http://localhost:8090" + baseHref)
            .baseBref(baseHref)
            .activeRequests(0l)
            .isHealthy(true)
            .recordedAt(new Date(Instant.now().toEpochMilli()))
            .build();

    when(this.serviceResolverService.resolveForBaseHref(eq(baseHref)))
        .thenReturn(List.of(instance));

    var bestInstance = loadBalancer.getBestInstanceForBaseHref(requestUri);

    assertTrue(bestInstance.isPresent());
    assertEquals("http://localhost:8090/test-app", bestInstance.get().uri());
    assertEquals(instance.getInstanceId(), bestInstance.get().instanceId());
  }

  @Test
  public void testResolveBestServiceForBaseHref_multipleInvokers() throws InterruptedException {
    var loadBalancer = new LoadBalancingService(this.serviceResolverService);

    var baseHref = "/test-app";
    var requestUri = "/test-app/marko/3";

    var instance =
        ResolvedInstance.builder()
            .instanceId(UUID.randomUUID().toString())
            .address("http://localhost:8090" + baseHref)
            .baseBref(baseHref)
            .activeRequests(0l)
            .isHealthy(true)
            .recordedAt(new Date(Instant.now().toEpochMilli()))
            .build();

    when(this.serviceResolverService.resolveForBaseHref(eq(baseHref)))
        .thenReturn(List.of(instance));

    var countDownLatch = new CountDownLatch(10);
    var executors = Executors.newFixedThreadPool(10);

    for (int i = 0; i < 10; i++) {
      executors.submit(
          () -> {
            for (int j = 0; j < 100; j++) {
              var bestInstance = loadBalancer.getBestInstanceForBaseHref(requestUri);

              assertTrue(bestInstance.isPresent());
              assertEquals("http://localhost:8090/test-app", bestInstance.get().uri());
              assertEquals(instance.getInstanceId(), bestInstance.get().instanceId());
            }
            countDownLatch.countDown();
          });
    }

    assertTrue(countDownLatch.await(10, java.util.concurrent.TimeUnit.SECONDS));
  }
}
