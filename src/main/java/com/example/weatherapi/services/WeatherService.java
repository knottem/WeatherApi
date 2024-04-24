package com.example.weatherapi.services;

import com.example.weatherapi.domain.weather.Weather;

public interface WeatherService {

    Weather getWeatherMerged(String city);

}