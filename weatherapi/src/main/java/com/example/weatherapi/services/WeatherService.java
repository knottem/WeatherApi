package com.example.weatherapi.services;

import org.springframework.http.ResponseEntity;

public interface WeatherService {
    ResponseEntity<Object> getWeatherByCity(String city);
}