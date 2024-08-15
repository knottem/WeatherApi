package com.example.weatherapi.exceptions;

public class InvalidApiUsageException extends RuntimeException {
    public InvalidApiUsageException(String message) {
        super(message);
    }
}
