package com.example.weatherapi.services.impl;

import com.example.weatherapi.api.SmhiApi;
import com.example.weatherapi.api.YrApi;
import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.services.CityService;
import com.example.weatherapi.services.WeatherService;
import com.example.weatherapi.util.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger LOG;
    private final Object lock = new Object();
    private Map<LocalDateTime, Weather.WeatherData> mergedWeatherData;
    private volatile int mergeCount;
    @Autowired
    public WeatherServiceImpl(CityService cityService, SmhiApi smhiApi, YrApi yrApi, Cache cache) {
        this.cityService = cityService;
        this.smhiApi = smhiApi;
        this.yrApi = yrApi;
        this.cache = cache;
        this.LOG = LoggerFactory.getLogger(WeatherServiceImpl.class);
    }

    @Override
    @Cacheable("cache")
    @Transactional
    public Weather getWeatherMerged(String cityName) {
        City city = toModel(cityService.getCityByName(cityName));
        Weather weatherFromCache = cache.getWeatherFromCache(city.getName());
        if(weatherFromCache != null) {
            return weatherFromCache;
        }
        resetMergedWeatherData();

        CompletableFuture<Weather> smhiFuture = smhiApi.fetchWeatherSmhiAsync(city.getLon(), city.getLat(), city);
        CompletableFuture<Weather> yrFuture = yrApi.fetchWeatherYrAsync(city.getLon(), city.getLat(), city);

        CompletableFuture<Void> combinedFuture = smhiFuture.thenCombineAsync(
                yrFuture,
                (smhiWeather, yrWeather) -> {
                    mergeWeatherDataIntoMergedData(smhiWeather.getWeatherData(), "SMHI");
                    mergeWeatherDataIntoMergedData(yrWeather.getWeatherData(), "YR");
                    return null;
                }
        );

        combinedFuture.join();

        Weather mergedWeather = Weather.builder()
                .message("Merged weather for " + city.getName())
                .weatherData(mergedWeatherData)
                .timestamp(LocalDateTime.now())
                .city(toModel(cityService.getCityByName(cityName)))
                .build();

        cache.save(city.getName(), mergedWeather);
        return mergedWeather;
    }

    private void mergeWeatherDataIntoMergedData(Map<LocalDateTime, Weather.WeatherData> newData, String api) {
        synchronized (lock) {
            for (Map.Entry<LocalDateTime, Weather.WeatherData> entry : newData.entrySet()) {
                LocalDateTime key = entry.getKey();
                Weather.WeatherData newDataItem = entry.getValue();

                if (mergedWeatherData.containsKey(key)) {
                    Weather.WeatherData existingData = mergedWeatherData.get(key);

                    float avgTemperature = ((existingData.getTemperature() * mergeCount) + newDataItem.getTemperature()) / (mergeCount + 1);
                    float avgWindDirection = ((existingData.getWindDirection() * mergeCount) + newDataItem.getWindDirection()) / (mergeCount + 1);
                    float avgWindSpeed = ((existingData.getWindSpeed() * mergeCount) + newDataItem.getWindSpeed()) / (mergeCount + 1);
                    float avgPrecipitation = ((existingData.getPrecipitation() * mergeCount) + newDataItem.getPrecipitation()) / (mergeCount + 1);

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
            mergeCount++;
            LOG.info("Merged weather data from {}", api);
        }
    }

    private void resetMergedWeatherData() {
        mergeCount = 1;
        mergedWeatherData = new TreeMap<>();
    }

}