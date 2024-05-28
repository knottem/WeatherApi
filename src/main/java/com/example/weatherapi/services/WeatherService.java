package com.example.weatherapi.services;

import com.example.weatherapi.domain.weather.Weather;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface WeatherService {

    ResponseEntity<Weather> fetchWeatherMergedResponse(String city);
    ResponseEntity<Weather> fetchWeatherMergedCustomApisResponse(String city, List<String> apis);

}