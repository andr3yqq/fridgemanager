package com.example.smartfridge.exceptions;

public class InviteNotOwnedException extends RuntimeException {
    public InviteNotOwnedException(String message) {
        super(message);
    }
}
