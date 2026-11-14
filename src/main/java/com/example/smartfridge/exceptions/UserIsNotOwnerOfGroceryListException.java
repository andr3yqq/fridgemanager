package com.example.smartfridge.exceptions;

public class UserIsNotOwnerOfGroceryListException extends RuntimeException {
    public UserIsNotOwnerOfGroceryListException(String message) {
        super(message);
    }
}
