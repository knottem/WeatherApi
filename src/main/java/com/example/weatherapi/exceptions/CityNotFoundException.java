package com.example.weatherapi.exceptions;

import org.springframework.web.context.request.WebRequest;

/**
 * Exception thrown when a city is not found in the database
 *
 * @see com.example.weatherapi.exceptions.handlers.CustomExceptionHandler#handleCityNotFoundException(CityNotFoundException, WebRequest)
 * For handling CityNotFoundException and mapping it to an appropriate HTTP response.
 */
public class CityNotFoundException extends RuntimeException {

    /**
     * Constructor for the exception
     * @param message the message to be displayed when the exception is thrown
     */
    public CityNotFoundException(String message) {
        super(message);
    }
}

