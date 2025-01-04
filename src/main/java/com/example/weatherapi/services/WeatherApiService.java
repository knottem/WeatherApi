package com.example.weatherapi.services;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.Weather;

public interface WeatherApiService {

    Weather fetchWeatherData(String apiName, City city, boolean smhiFlag, boolean yrFlag, boolean fmiFlag, boolean validateApiStatus);
    void saveWeatherData(String apiName, Weather weather, boolean smhiFlag, boolean yrFlag, boolean fmiFlag);

}
