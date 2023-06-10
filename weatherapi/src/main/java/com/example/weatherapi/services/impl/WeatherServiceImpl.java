package com.example.weatherapi.services.impl;

import com.example.weatherapi.api.SmhiApi;
import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.WeatherYr;
import com.example.weatherapi.services.CityService;
import com.example.weatherapi.services.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class WeatherServiceImpl implements WeatherService {

    private final CityService cityService;
    private final SmhiApi smhiApi;

    public WeatherServiceImpl(CityService cityService) {
        this.cityService = cityService;
        this.smhiApi = new SmhiApi();
    }

    @Override
    public ResponseEntity<Object> getWeatherByCity(String city) {
        City cityObject = cityService.getCityByName(city);
        return smhiApi.getWeatherByCity(cityObject);
    }

    @Override
    public WeatherYr getWeatherByYrCity(String city) {
        City cityObject = cityService.getCityByName(city);
        return smhiApi.getWeatherYr(cityObject.getLon(), cityObject.getLat());
    }
}