package com.example.weatherapi.exceptions;

public class WeatherNotFilledException extends RuntimeException{

    public WeatherNotFilledException(String message) {
        super(message);
    }
}
