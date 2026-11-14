package com.example.smartfridge.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserDoesNotHaveFridgeException extends RuntimeException {
    public UserDoesNotHaveFridgeException(String message) {
        super(message);
    }
}
