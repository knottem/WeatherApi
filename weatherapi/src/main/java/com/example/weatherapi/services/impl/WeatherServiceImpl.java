package com.example.weatherapi.services.impl;

import com.example.weatherapi.api.SmhiApi;
import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.services.CityService;
import com.example.weatherapi.services.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Weather getWeatherBySmhiCity(String city) {
        City cityObject = cityService.getCityByName(city);
        return smhiApi.getWeatherByCity(cityObject);
    }

    @Override
    public Weather getWeatherByYrCity(String city) {
        City cityObject = cityService.getCityByName(city);
        return smhiApi.getWeatherYrByCity(cityObject.getLon(), cityObject.getLat(), cityObject);
    }

    @Override
    public Weather getWeatherMerged(String city) {
        City cityObject = cityService.getCityByName(city);
        return smhiApi.mergeWeather(cityObject);
    }

}