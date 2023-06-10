package com.example.weatherapi.services.impl;

import com.example.weatherapi.api.SmhiApi;
import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.WeatherYr;
import com.example.weatherapi.services.CityService;
import com.example.weatherapi.services.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class WeatherServiceImpl implements WeatherService {

    private final CityService cityService;
    private final SmhiApi smhiApi;

    @Autowired
    public WeatherServiceImpl(CityService cityService, SmhiApi smhiApi) {
        this.cityService = cityService;
        this.smhiApi = smhiApi;
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