package com.example.weatherapi.services.impl;

import com.example.weatherapi.api.SmhiApi;
import com.example.weatherapi.api.YrApi;
import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.services.CityService;
import com.example.weatherapi.services.WeatherService;
import com.example.weatherapi.util.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import static com.example.weatherapi.util.CityMapper.toModel;

@Service
public class WeatherServiceImpl implements WeatherService {

    private final CityService cityService;
    private final SmhiApi smhiApi;
    private final YrApi yrApi;
    private final Cache cache;

    private final Object lock = new Object();
    private Map<LocalDateTime, Weather.WeatherData> mergedWeatherData;

    private volatile int mergeCount;
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
    @Cacheable("cache")
    @Transactional
    public Weather getWeatherMerged(String cityName) {
        City city = toModel(cityService.getCityByName(cityName));
        String key = city.getLon() + ":" + city.getLat() + ":merged";
        Weather weatherFromCache = cache.getWeatherFromCache(key);
        if(weatherFromCache != null) {
            return weatherFromCache;
        }
        resetMergedWeatherData();

        CompletableFuture<Weather> smhiFuture = smhiApi.fetchWeatherSmhiAsync(city.getLon(), city.getLat(), city);
        CompletableFuture<Weather> yrFuture = yrApi.fetchWeatherYrAsync(city.getLon(), city.getLat(), city);

        CompletableFuture<Void> combinedFuture = smhiFuture.thenCombineAsync(
                yrFuture,
                (smhiWeather, yrWeather) -> {
                    // Merge the weather data here
                    mergeWeatherDataIntoMergedData(smhiWeather.getWeatherData());
                    mergeWeatherDataIntoMergedData(yrWeather.getWeatherData());
                    return null;
                }
        );

        combinedFuture.join();

        Weather mergedWeather = Weather.builder()
                .message("Merged weather for " + city.getName() + " with location Lon: " + city.getLon() + " and Lat: " + city.getLat())
                .weatherData(mergedWeatherData)
                .timeStamp(LocalDateTime.now())
                .city(city)
                .build();

        cache.save(key, mergedWeather);
        return mergedWeather;
    }

    private void mergeWeatherDataIntoMergedData(Map<LocalDateTime, Weather.WeatherData> newData) {
        synchronized (lock) {
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
                            .temperature(BigDecimal.valueOf(avgTemperature).setScale(2, RoundingMode.HALF_UP).floatValue())
                            .windDirection(BigDecimal.valueOf(avgWindDirection).setScale(2, RoundingMode.HALF_UP).floatValue())
                            .windSpeed(BigDecimal.valueOf(avgWindSpeed).setScale(2, RoundingMode.HALF_UP).floatValue())
                            .precipitation(BigDecimal.valueOf(avgPrecipitation).setScale(2, RoundingMode.HALF_UP).floatValue())
                            .weatherCode(existingData.getWeatherCode())
                            .build();

                    mergedWeatherData.put(key, mergedData);
                } else {
                    mergedWeatherData.put(key, newDataItem);
                }
            }
        }
    }

    private void resetMergedWeatherData() {
        mergeCount = 1;
        mergedWeatherData = new TreeMap<>();
    }

}