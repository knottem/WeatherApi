package com.example.weatherapi.services;

import com.example.weatherapi.domain.weather.Weather;
import org.springframework.http.ResponseEntity;

public interface WeatherService {

    ResponseEntity<Weather> fetchWeatherMergedResponse(String city);

}