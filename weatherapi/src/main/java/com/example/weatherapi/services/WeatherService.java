package com.example.weatherapi.services;

import com.example.weatherapi.domain.weather.Weather;

public interface WeatherService {
    Weather getWeatherBySmhiCity(String city);
    Weather getWeatherByYrCity(String city);
    Weather getWeatherMerged(String city);
}