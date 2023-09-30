package com.example.weatherapi.services.impl;

import com.example.weatherapi.api.SmhiApi;
import com.example.weatherapi.api.YrApi;
import com.example.weatherapi.domain.City;
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

import static com.example.weatherapi.util.CityMapper.toModel;

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
    public Weather getWeatherBySmhiCity(String cityName) {
        City city = toModel(cityService.getCityByName(cityName));
        return smhiApi.getWeatherSmhi(city.getLon(), city.getLat(), city);
    }

    @Override
    public Weather getWeatherByYrCity(String cityName) {
        City city = toModel(cityService.getCityByName(cityName));
        return yrApi.getWeatherYr(city.getLon(), city.getLat(), city);
    }

    @Override
    public Weather getWeatherMerged(String cityName) {
        City city = toModel(cityService.getCityByName(cityName));
        Weather weatherFromCache = Cache.getInstance().getWeatherFromCache(city.getName() + "_merged", CACHE_TIME_IN_HOURS);
        if(weatherFromCache != null) {
            return weatherFromCache;
        }
        Weather weatherYr = yrApi.getWeatherYr(city.getLon(), city.getLat(), city);
        Weather weatherSmhi = smhiApi.getWeatherSmhi(city.getLon(), city.getLat(), city);

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
                .message("Merged weather for " + city.getName() + " with location Lon: " + city.getLon() + " and Lat: " + city.getLat())
                .weatherData(mergedWeatherData)
                .build();

        Cache.getInstance().put(city.getName() + "_merged", mergedWeather);
        return mergedWeather;
    }

}