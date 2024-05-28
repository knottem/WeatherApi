package com.example.weatherapi.services;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.Weather;

public interface WeatherApiService {

    Weather fetchWeatherData(String apiName, City city, boolean smhiFlag, boolean yrFlag, boolean fmiFlag);
    Weather fetchWeatherDataCached(String apiName, City city);
    void saveWeatherData(String apiName, Weather weather, boolean smhiFlag, boolean yrFlag, boolean fmiFlag);

}
