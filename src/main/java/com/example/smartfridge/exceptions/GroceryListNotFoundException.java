package com.example.smartfridge.exceptions;

public class GroceryListNotFoundException extends RuntimeException {
    public GroceryListNotFoundException(String message) {
        super(message);
    }
}
