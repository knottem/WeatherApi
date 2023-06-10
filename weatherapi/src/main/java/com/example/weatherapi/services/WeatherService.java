package com.example.weatherapi.services;

import com.example.weatherapi.domain.weather.WeatherYr;
import org.springframework.http.ResponseEntity;

public interface WeatherService {
    ResponseEntity<Object> getWeatherBySmhiCity(String city);

    WeatherYr getWeatherByYrCity(String city);
}