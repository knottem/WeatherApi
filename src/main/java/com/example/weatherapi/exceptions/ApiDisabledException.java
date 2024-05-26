package com.example.weatherapi.exceptions;

public class ApiDisabledException extends RuntimeException {
    public ApiDisabledException(String message) {
        super(message);
    }
}

