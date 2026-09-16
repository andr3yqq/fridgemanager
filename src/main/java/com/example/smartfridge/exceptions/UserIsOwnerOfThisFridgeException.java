package com.example.smartfridge.exceptions;

public class UserIsOwnerOfThisFridgeException extends RuntimeException {
    public UserIsOwnerOfThisFridgeException(String message) {
        super(message);
    }
}
