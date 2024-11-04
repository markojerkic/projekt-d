package dev.jerkicv.docker;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;

@SpringBootApplication
@Slf4j
public class DockerApplication {

    @Value("${target.server.address}")
    private String serverAddress;
    @Value("${num.threads}")
    private long numThreads;
    @Value("${num.seconds}")
    private long numSeconds;

    public static void main(String[] args) {
        SpringApplication.run(DockerApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {

            log.info("Server address: {}", serverAddress);
            var restTemplate = new RestTemplate();

            var responseTimes = new ConcurrentLinkedQueue<Long>();
            var statuses = new ConcurrentLinkedQueue<HttpStatusCode>();

            var countDownLatch = new CountDownLatch((int) numThreads);
            var executorService = Executors.newFixedThreadPool((int) numThreads);

            for (int i = 0; i < numThreads; i++) {
                executorService.submit(() -> {
                    long startTime = System.currentTimeMillis();
                    long endTime = startTime + numSeconds * 1000;
                    while (System.currentTimeMillis() < endTime) {
                        long requestStartTime = System.currentTimeMillis();
                        try {
                            var response = restTemplate.getForEntity(serverAddress, String.class);
                            long requestEndTime = System.currentTimeMillis();
                            responseTimes.add(requestEndTime - requestStartTime);
                            statuses.add(response.getStatusCode());
                        } catch (Exception e) {
                            statuses.add(HttpStatusCode.valueOf(500));
                        }
                    }

                    countDownLatch.countDown();
                });
            }

            countDownLatch.await();
            log.info("All threads finished");

            // Print min, max, avg, mean response times
            long min = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);
            long max = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
            double avg = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
            double mean = responseTimes.stream().mapToLong(Long::longValue).sum() / responseTimes.size();
            System.out.println("=====================================");
            log.info("Min response time: {}ms", min);
            log.info("Max response time: {}ms", max);
            log.info("Avg response time: {}ms", avg);
            log.info("Mean response time: {}ms", mean);
            System.out.println("=====================================");

            // Print success rate
            long successCount = statuses.stream().filter(status -> status.is2xxSuccessful()).count();
            double successRate = (double) successCount / statuses.size();
            log.info("Success rate: {}", successRate);
            System.out.println("=====================================");

            // Shutdown the application
            System.exit(0);
        };
    }

}
