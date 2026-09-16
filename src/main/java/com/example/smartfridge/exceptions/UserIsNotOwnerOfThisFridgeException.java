package com.example.smartfridge.exceptions;

public class UserIsNotOwnerOfThisFridgeException extends RuntimeException {
    public UserIsNotOwnerOfThisFridgeException(String message) {
        super(message);
    }
}
