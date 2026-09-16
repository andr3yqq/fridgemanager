package com.example.smartfridge.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserDoesNotHaveAFridgeException extends RuntimeException {
    public UserDoesNotHaveAFridgeException(String message) {
        super(message);
    }
}
