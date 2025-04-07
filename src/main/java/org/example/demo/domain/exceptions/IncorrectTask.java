package org.example.demo.domain.exceptions;

public class IncorrectTask extends RuntimeException {
    public IncorrectTask(String message) {
        super(message);
    }
}
