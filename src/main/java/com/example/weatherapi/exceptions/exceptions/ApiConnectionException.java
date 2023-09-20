package com.example.weatherapi.exceptions.exceptions;

// A custom exception class for handling API connection errors
public class ApiConnectionException extends RuntimeException {
    public ApiConnectionException(String message) {
        super(message);
    }
}
