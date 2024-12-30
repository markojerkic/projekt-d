package dev.jerkic.custom_load_balancer.discovery_server.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(reason = "No instance found for the given base href", code = HttpStatus.NOT_FOUND)
public class NoInstanceFoundException extends RuntimeException {
  public NoInstanceFoundException(String message) {
    super(message);
  }
}
