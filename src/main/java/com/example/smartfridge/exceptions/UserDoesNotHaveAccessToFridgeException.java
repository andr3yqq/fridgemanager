package com.example.smartfridge.exceptions;

public class UserDoesNotHaveAccessToFridgeException extends RuntimeException {
    public UserDoesNotHaveAccessToFridgeException(String message) {
        super(message);
    }
}
