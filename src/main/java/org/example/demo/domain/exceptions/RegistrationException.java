package org.example.demo.domain.exceptions;

public class RegistrationException extends RuntimeException {
  public RegistrationException(String message) {
    super(message);
  }
}
