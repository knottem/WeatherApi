package com.example.weatherapi.services.impl;

import com.example.weatherapi.api.SmhiApi;
import com.example.weatherapi.api.YrApi;
import com.example.weatherapi.domain.entities.CityEntity;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.services.CityService;
import com.example.weatherapi.services.WeatherService;
import com.example.weatherapi.util.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

@Service
public class WeatherServiceImpl implements WeatherService {

    private final CityService cityService;
    private final SmhiApi smhiApi;
    private final YrApi yrApi;

    @Value("${cache.time.in.hours}")
    private int CACHE_TIME_IN_HOURS;
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
    public Weather getWeatherMerged(String cityIn) {
        String city = cityIn.toLowerCase();
        Weather weatherFromCache = Cache.getInstance().getWeatherFromCache(city + "_merged", CACHE_TIME_IN_HOURS);
        if(weatherFromCache != null) {
            return weatherFromCache;
        }
        CityEntity cityEntityObject = cityService.getCityByName(city);
        Weather weatherYr = yrApi.getWeatherYr(cityEntityObject.getLon(), cityEntityObject.getLat(), cityEntityObject);
        Weather weatherSmhi = smhiApi.getWeatherSmhi(cityEntityObject.getLon(), cityEntityObject.getLat(), cityEntityObject);

        Map<LocalDateTime, Weather.WeatherData> smhiWeatherData = weatherSmhi.getWeatherData();
        Map<LocalDateTime, Weather.WeatherData> yrWeatherData = weatherYr.getWeatherData();

        // Add all smhi data to the merged map from the start, then we can just add yr data to the merged map
        Map<LocalDateTime, Weather.WeatherData> mergedWeatherData = new TreeMap<>(smhiWeatherData);

        for (Map.Entry<LocalDateTime, Weather.WeatherData> entry : yrWeatherData.entrySet()) {
            LocalDateTime key = entry.getKey();
            Weather.WeatherData yrData = entry.getValue();

            if (mergedWeatherData.containsKey(key)) {
                Weather.WeatherData smhiData = mergedWeatherData.get(key);

                float avgTemperature = (smhiData.getTemperature() + yrData.getTemperature()) / 2;
                float avgWindDirection = (smhiData.getWindDirection() + yrData.getWindDirection()) / 2;
                float avgWindSpeed = (smhiData.getWindSpeed() + yrData.getWindSpeed()) / 2;
                float avgPrecipitation = (smhiData.getPrecipitation() + yrData.getPrecipitation()) / 2;

                Weather.WeatherData newData = Weather.WeatherData.builder()
                        .temperature(avgTemperature)
                        .windDirection(avgWindDirection)
                        .windSpeed(avgWindSpeed)
                        .precipitation(avgPrecipitation)
                        .weatherCode(smhiData.getWeatherCode())
                        .build();

                mergedWeatherData.put(key, newData);
            } else {
                mergedWeatherData.put(key, yrData);
            }
        }
        Weather mergedWeather = Weather.builder()
                .message("Merged weather for " + cityEntityObject.getName() + " with location Lon: " + cityEntityObject.getLon() + " and Lat: " + cityEntityObject.getLat())
                .weatherData(mergedWeatherData)
                .build();

        Cache.getInstance().put(city + "merged", mergedWeather);
        return mergedWeather;
    }

}