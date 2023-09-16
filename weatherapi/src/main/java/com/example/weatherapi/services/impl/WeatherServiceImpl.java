package com.example.weatherapi.services.impl;

import com.example.weatherapi.api.SmhiApi;
import com.example.weatherapi.api.YrApi;
import com.example.weatherapi.domain.entities.CityEntity;
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
        CityEntity cityEntityObject = cityService.getCityByName(city);
        return smhiApi.getWeatherSmhi(cityEntityObject.getLon(), cityEntityObject.getLat(), cityEntityObject);
    }

    @Override
    public Weather getWeatherByYrCity(String city) {
        CityEntity cityEntityObject = cityService.getCityByName(city);
        return yrApi.getWeatherYr(cityEntityObject.getLon(), cityEntityObject.getLat(), cityEntityObject);
    }

    @Override
    public Weather getWeatherMerged(String city) {
        CityEntity cityEntityObject = cityService.getCityByName(city);
        Weather weatherYr = yrApi.getWeatherYr(cityEntityObject.getLon(), cityEntityObject.getLat(), cityEntityObject);
        Weather weatherSmhi = smhiApi.getWeatherSmhi(cityEntityObject.getLon(), cityEntityObject.getLat(), cityEntityObject);

        Map<LocalDateTime, Weather.WeatherData> smhiWeatherData = weatherSmhi.getWeatherData();
        Map<LocalDateTime, Weather.WeatherData> yrWeatherData = weatherYr.getWeatherData();

        //Add all smhi data to the merged map from the start, then we can just add yr data to the merged map
        Map<LocalDateTime, Weather.WeatherData> mergedWeatherData = new TreeMap<>(smhiWeatherData);

        for (Map.Entry<LocalDateTime, Weather.WeatherData> entry : yrWeatherData.entrySet()) {
            LocalDateTime key = entry.getKey();
            Weather.WeatherData yrData = entry.getValue();
            // If the key already exists in the merged map, we need to merge the data, otherwise we just add it
            if (mergedWeatherData.containsKey(key)) {
                Weather.WeatherData data = mergedWeatherData.get(key);
                data.setTemperature((data.getTemperature() + yrData.getTemperature()) / 2);
                data.setWindDirection((data.getWindDirection() + yrData.getWindDirection()) / 2);
                data.setWindSpeed((data.getWindSpeed() + yrData.getWindSpeed()) / 2);
                data.setPrecipitation((data.getPrecipitation() + yrData.getPrecipitation()) / 2);
            } else {
                mergedWeatherData.put(key, yrData);
            }
        }

        return Weather.builder()
                .message("Merged weather for " + cityEntityObject.getName() + " with location Lon: " + cityEntityObject.getLon() + " and Lat: " + cityEntityObject.getLat())
                .weatherData(mergedWeatherData)
                .build();

    }

}