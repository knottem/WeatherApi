package com.example.weatherapi.services.impl;

import com.example.weatherapi.api.SmhiApi;
import com.example.weatherapi.api.YrApi;
import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.services.CityService;
import com.example.weatherapi.services.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

@Service
public class WeatherServiceImpl implements WeatherService {

    private final CityService cityService;
    private final SmhiApi smhiApi;
    private final YrApi yrApi;

    @Autowired
    public WeatherServiceImpl(CityService cityService, SmhiApi smhiApi, YrApi yrApi) {
        this.cityService = cityService;
        this.smhiApi = smhiApi;
        this.yrApi = yrApi;
    }

    @Override
    public Weather getWeatherBySmhiCity(String city) {
        City cityObject = cityService.getCityByName(city);
        return smhiApi.getWeatherSmhi(cityObject.getLon(), cityObject.getLat(), cityObject);
    }

    @Override
    public Weather getWeatherByYrCity(String city) {
        City cityObject = cityService.getCityByName(city);
        return yrApi.getWeatherYr(cityObject.getLon(), cityObject.getLat(), cityObject);
    }

    @Override
    public Weather getWeatherMerged(String city) {
        City cityObject = cityService.getCityByName(city);
        Weather weatherYr = yrApi.getWeatherYr(cityObject.getLon(), cityObject.getLat(), cityObject);
        Weather weatherSmhi = smhiApi.getWeatherSmhi(cityObject.getLon(), cityObject.getLat(), cityObject);

        Map<LocalDateTime, Float> smhiTemperatures = weatherSmhi.getTemperatures();
        Map<LocalDateTime, Float> yrTemperatures = weatherYr.getTemperatures();

        Map<LocalDateTime, Float> mergedTemperatures = new TreeMap<>(smhiTemperatures);
        for (Map.Entry<LocalDateTime, Float> entry : yrTemperatures.entrySet()) {
            LocalDateTime key = entry.getKey();
            Float yrValue = entry.getValue();
            if (mergedTemperatures.containsKey(key)) {
                mergedTemperatures.put(key, (mergedTemperatures.get(key) + yrValue)/2);
            } else {
                mergedTemperatures.put(key, yrValue);
            }
        }

        return Weather.builder()
                .message("Merged weather for " + cityObject.getName() + " with location Lon: " + cityObject.getLon() + " and Lat: " + cityObject.getLat())
                .temperatures(mergedTemperatures)
                .build();

    }

}