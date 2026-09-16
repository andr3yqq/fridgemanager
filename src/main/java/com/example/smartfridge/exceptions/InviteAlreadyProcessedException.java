package com.example.smartfridge.exceptions;

public class InviteAlreadyProcessedException extends RuntimeException {
    public InviteAlreadyProcessedException(String message) {
        super(message);
    }
}
