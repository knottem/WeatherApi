package com.example.weatherapi.services.impl;

import com.example.weatherapi.api.SmhiApi;
import com.example.weatherapi.api.YrApi;
import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.services.CityService;
import com.example.weatherapi.services.WeatherService;
import com.example.weatherapi.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import static com.example.weatherapi.util.CityMapper.toModel;
import static com.example.weatherapi.util.SunriseUtil.getSunriseSunset;

@Service
public class WeatherServiceImpl implements WeatherService {

    private final CityService cityService;
    private final SmhiApi smhiApi;
    private final YrApi yrApi;
    private final Cache cache;
    private final Logger log;
    private Map<ZonedDateTime, Weather.WeatherData> mergedWeatherData;
    private volatile int mergeCount;
    @Autowired
    public WeatherServiceImpl(CityService cityService, SmhiApi smhiApi, YrApi yrApi, Cache cache) {
        this.cityService = cityService;
        this.smhiApi = smhiApi;
        this.yrApi = yrApi;
        this.cache = cache;
        this.log = LoggerFactory.getLogger(WeatherServiceImpl.class);
    }

    @Override
    @Cacheable("cache")
    public Weather getWeatherMerged(String cityName) {
        City city = toModel(cityService.getCityByName(cityName));
        Weather weatherFromCache = cache.getWeatherFromCache(city.getName());
        if(weatherFromCache != null) {
            getSunriseSunset(weatherFromCache);
            return weatherFromCache;
        }
        mergeCount = 1;
        mergedWeatherData = new TreeMap<>();

        CompletableFuture<Void> smhi = fetchAndProcessWeatherData("SMHI", smhiApi.fetchWeatherSmhiAsync(city));
        CompletableFuture<Void> yr = fetchAndProcessWeatherData("YR", yrApi.fetchWeatherYrAsync(city));
        CompletableFuture.allOf(smhi,yr).join();

        setScaleWeatherData();

        Weather mergedWeather = Weather.builder()
                .message("Merged weather for " + city.getName() + " from SMHI and YR")
                .weatherData(mergedWeatherData)
                .timestamp(ZonedDateTime.now(ZoneId.of("UTC")))
                .city(city)
                .build();

        mergedWeather.getWeatherData().entrySet().removeIf(entry -> entry.getValue().getWeatherCode() == -1);

        cache.save(mergedWeather);
        getSunriseSunset(mergedWeather);
        return mergedWeather;
    }

    private CompletableFuture<Void> fetchAndProcessWeatherData(String apiName, CompletableFuture<Weather> weatherFuture) {
        return weatherFuture
                .exceptionally(e -> {
                    log.error("Failed to fetch weather data from " + apiName, e);
                    return null;
                })
                .thenAccept(weather -> {
                    if (weather != null) {
                        mergeWeatherDataIntoMergedData(weather.getWeatherData(), apiName);
                    }
                });
    }

    private synchronized void mergeWeatherDataIntoMergedData(Map<ZonedDateTime, Weather.WeatherData> newData, String api) {
        for (Map.Entry<ZonedDateTime, Weather.WeatherData> entry : newData.entrySet()) {
            ZonedDateTime key = entry.getKey();
            Weather.WeatherData newDataItem = entry.getValue();

            if (mergedWeatherData.containsKey(key)) {
                Weather.WeatherData existingData = mergedWeatherData.get(key);

                float avgTemperature = ((existingData.getTemperature() * mergeCount) + newDataItem.getTemperature()) / (mergeCount + 1);
                float avgWindDirection = getAvgWindDirection(existingData, newDataItem);
                float avgWindSpeed = ((existingData.getWindSpeed() * mergeCount) + newDataItem.getWindSpeed()) / (mergeCount + 1);
                float avgPrecipitation = ((existingData.getPrecipitation() * mergeCount) + newDataItem.getPrecipitation()) / (mergeCount + 1);

                int weatherCode;
                if (api.equals("SMHI")) {
                    weatherCode = newDataItem.getWeatherCode();
                } else {
                    weatherCode = existingData.getWeatherCode() > -1 ? existingData.getWeatherCode() : newDataItem.getWeatherCode();
                }

                Weather.WeatherData mergedData = Weather.WeatherData.builder()
                        .temperature(avgTemperature)
                        .windDirection(avgWindDirection)
                        .windSpeed(avgWindSpeed)
                        .precipitation(avgPrecipitation)
                        .weatherCode(weatherCode)
                        .build();

                mergedWeatherData.put(key, mergedData);
            } else {
                mergedWeatherData.put(key, newDataItem);
            }
        }
        mergeCount++;
        log.info("Merged weather data from {}", api);
    }

    private float getAvgWindDirection(Weather.WeatherData existingData, Weather.WeatherData newDataItem) {
        double existingX = Math.cos(Math.toRadians(existingData.getWindDirection())) * mergeCount;
        double existingY = Math.sin(Math.toRadians(existingData.getWindDirection())) * mergeCount;
        double newX = Math.cos(Math.toRadians(newDataItem.getWindDirection()));
        double newY = Math.sin(Math.toRadians(newDataItem.getWindDirection()));
        double avgX = (existingX + newX) / (mergeCount + 1);
        double avgY = (existingY + newY) / (mergeCount + 1);
        float avgWindDirection = (float) Math.toDegrees(Math.atan2(avgY, avgX));
        if (avgWindDirection < 0) {
            avgWindDirection += 360;
        }
        return avgWindDirection;
    }

    private void setScaleWeatherData(){
        for (Map.Entry<ZonedDateTime, Weather.WeatherData> entry : mergedWeatherData.entrySet()) {
            Weather.WeatherData weatherData = entry.getValue();
            weatherData.setTemperature(BigDecimal.valueOf(weatherData.getTemperature()).setScale(1, RoundingMode.HALF_UP).floatValue());
            weatherData.setWindDirection(BigDecimal.valueOf(weatherData.getWindDirection()).setScale(1, RoundingMode.HALF_UP).floatValue());
            weatherData.setWindSpeed(BigDecimal.valueOf(weatherData.getWindSpeed()).setScale(1, RoundingMode.HALF_UP).floatValue());
            weatherData.setPrecipitation(BigDecimal.valueOf(weatherData.getPrecipitation()).setScale(1, RoundingMode.HALF_UP).floatValue());
        }
    }

}