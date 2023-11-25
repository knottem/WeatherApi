package com.example.weatherapi.services.impl;

import com.example.weatherapi.api.SmhiApi;
import com.example.weatherapi.api.YrApi;
import com.example.weatherapi.domain.City;
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

    private final Cache cache;

    @Value("${cache.time.in.hours}")
    private int CACHE_TIME_IN_HOURS;

    private Map<LocalDateTime, Weather.WeatherData> mergedWeatherData;

    private int mergeCount = 0;
    @Autowired
    public WeatherServiceImpl(CityService cityService, SmhiApi smhiApi, YrApi yrApi, Cache cache) {
        this.cityService = cityService;
        this.smhiApi = smhiApi;
        this.yrApi = yrApi;
        this.cache = cache;
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
        String key = city.getLon() + ":" + city.getLat() + ":merged";
        Weather weatherFromCache = cache.getWeatherFromCache(key, CACHE_TIME_IN_HOURS);
        if(weatherFromCache != null) {
            return weatherFromCache;
        }
        // Reset the merge count and the merged weather data
        mergeCount = 0;
        mergedWeatherData = new TreeMap<>();

        // Merge the weather data from the two APIs
        mergeWeatherDataIntoMergedData(smhiApi.getWeatherSmhi(city.getLon(), city.getLat(), city).getWeatherData());
        mergeWeatherDataIntoMergedData(yrApi.getWeatherYr(city.getLon(), city.getLat(), city).getWeatherData());


        Weather mergedWeather = Weather.builder()
                .message("Merged weather for " + city.getName() + " with location Lon: " + city.getLon() + " and Lat: " + city.getLat())
                .weatherData(mergedWeatherData)
                .timeStamp(LocalDateTime.now())
                .build();

        cache.save(key, mergedWeather);
        return mergedWeather;
    }

    private void mergeWeatherDataIntoMergedData(Map<LocalDateTime, Weather.WeatherData> newData) {
        for (Map.Entry<LocalDateTime, Weather.WeatherData> entry : newData.entrySet()) {
            LocalDateTime key = entry.getKey();
            Weather.WeatherData newDataItem = entry.getValue();

            if (mergedWeatherData.containsKey(key)) {
                Weather.WeatherData existingData = mergedWeatherData.get(key);

                int totalCount = mergeCount + 1;
                float avgTemperature = ((existingData.getTemperature() * mergeCount) + newDataItem.getTemperature()) / totalCount;
                float avgWindDirection = ((existingData.getWindDirection() * mergeCount) + newDataItem.getWindDirection()) / totalCount;
                float avgWindSpeed = ((existingData.getWindSpeed() * mergeCount) + newDataItem.getWindSpeed()) / totalCount;
                float avgPrecipitation = ((existingData.getPrecipitation() * mergeCount) + newDataItem.getPrecipitation()) / totalCount;

                Weather.WeatherData mergedData = Weather.WeatherData.builder()
                        .temperature(avgTemperature)
                        .windDirection(avgWindDirection)
                        .windSpeed(avgWindSpeed)
                        .precipitation(avgPrecipitation)
                        .weatherCode(existingData.getWeatherCode())
                        .build();

                mergedWeatherData.put(key, mergedData);
            } else {
                mergedWeatherData.put(key, newDataItem);
            }
        }
    }

}