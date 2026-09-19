package com.example.smartfridge.exceptions;

public class UserIsNotOwnerOfFridgeException extends RuntimeException {
    public UserIsNotOwnerOfFridgeException(String message) {
        super(message);
    }
}
