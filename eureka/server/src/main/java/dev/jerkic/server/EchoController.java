package dev.jerkic.server;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EchoController {

    @GetMapping
    public ResponseEntity<String> echo() {

        // Generate a random response time
        int responseTime = (int) (Math.random() * 2000);

        // Randomly throw an exception
        double random = Math.random();
        if (random < 0.1) {
            return ResponseEntity.status(500).body("Random exception");
        }

        // Sleep for the response time
        try {
            Thread.sleep(responseTime);
        } catch (InterruptedException e) {
            return ResponseEntity.status(504).body("Sleep interrupted");
        }

        return ResponseEntity.ok().body("Echo: " + random);
    }

}
