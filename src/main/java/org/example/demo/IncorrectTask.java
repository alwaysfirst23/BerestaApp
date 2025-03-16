package org.example.demo;

public class IncorrectTask extends RuntimeException {
    public IncorrectTask(String message) {
        super(message);
    }
}
