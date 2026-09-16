package com.example.smartfridge.utils;

import com.example.smartfridge.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({UsernameNotFoundException.class, FridgeNotFoundException.class, ItemNotFoundException.class, UserDoesNotHaveAFridgeException.class})
    public ProblemDetail handleNotFoundException(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception ex) {
        LOGGER.error("Unexpected error occurred", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    @ExceptionHandler({InviteAlreadyProcessedException.class, InviteConflictException.class, UserAlreadyExistsException.class, UserAlreadyHasFridgeException.class})
    public ProblemDetail handleConflictException(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({InviteNotOwnedException.class, UserIsNotOwnerOfThisFridgeException.class, UserIsOwnerOfThisFridgeException.class})
    public ProblemDetail handleForbiddenException(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

}

