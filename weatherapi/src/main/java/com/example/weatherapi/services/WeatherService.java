package com.example.weatherapi.services;

import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.domain.weather.WeatherYr;

public interface WeatherService {
    Weather getWeatherBySmhiCity(String city);
    WeatherYr getWeatherByYrCity(String city);
}