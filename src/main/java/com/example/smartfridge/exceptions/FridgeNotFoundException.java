package com.example.smartfridge.exceptions;

public class FridgeNotFoundException extends RuntimeException {
    public FridgeNotFoundException(String message) {
        super(message);
    }
}
