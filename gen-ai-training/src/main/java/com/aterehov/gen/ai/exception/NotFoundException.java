package com.aterehov.gen.ai.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class NotFoundException extends ResponseStatusException {

    public static final String DEFAULT_MESSAGE = "Resource not found";

    public NotFoundException() {
        super(HttpStatus.NOT_FOUND, DEFAULT_MESSAGE);
    }

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}