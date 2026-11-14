package com.example.smartfridge.exceptions;

public class GroceryItemNotFoundException extends RuntimeException {
    public GroceryItemNotFoundException(String message) {
        super(message);
    }
}
