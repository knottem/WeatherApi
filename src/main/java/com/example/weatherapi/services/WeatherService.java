package com.example.weatherapi.services;

import com.example.weatherapi.domain.weather.Weather;
import org.springframework.cache.annotation.Cacheable;

public interface WeatherService {

    Weather getWeatherMerged(String city);

}