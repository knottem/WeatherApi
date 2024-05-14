package com.example.weatherapi.services.impl;

import com.example.weatherapi.api.SmhiApi;
import com.example.weatherapi.api.YrApi;
import com.example.weatherapi.cache.CacheDB;
import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.exceptions.WeatherNotFilledException;
import com.example.weatherapi.services.CityService;
import com.example.weatherapi.services.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import static com.example.weatherapi.util.CityMapper.toModel;
import static com.example.weatherapi.util.SunriseUtil.getSunriseSunset;

@Service
public class WeatherServiceImpl implements WeatherService {

    private final CityService cityService;
    private final SmhiApi smhiApi;
    private final YrApi yrApi;
    private final CacheDB cacheDB;
    private final Logger log;
    private Map<ZonedDateTime, Weather.WeatherData> mergedWeatherData;
    private volatile int mergeCount;
    private final CacheManager cacheManager;
    private final String cacheName;


    @Autowired
    public WeatherServiceImpl(CityService cityService, SmhiApi smhiApi, YrApi yrApi, CacheDB cacheDB, CacheManager cacheManager) {
        this.cityService = cityService;
        this.smhiApi = smhiApi;
        this.yrApi = yrApi;
        this.cacheDB = cacheDB;
        this.cacheManager = cacheManager;
        this.log = LoggerFactory.getLogger(WeatherServiceImpl.class);
        this.cacheName = "cache";
    }

    @Override
    public Weather getWeatherMerged(String cityName) {
        String key = cityName.toLowerCase() + "merged";
        Weather weatherFromCache = Objects.requireNonNull(cacheManager.getCache(cacheName))
                .get(key, Weather.class);
        if(weatherFromCache != null) {
            log.info("Cache hit for City: {} in the cache, returning cached data", cityName);
            return weatherFromCache;
        }

        City city = toModel(cityService.getCityByName(cityName));
        Weather weatherFromCacheDB = cacheDB
                .getWeatherFromCache(city.getName(), true, true);
        if(weatherFromCacheDB != null) {
            getSunriseSunset(weatherFromCacheDB);
            Objects.requireNonNull(cacheManager.getCache(cacheName))
                    .put(key, weatherFromCacheDB);
            return weatherFromCacheDB;
        }

        this.mergeCount = 0;
        this.mergedWeatherData = new TreeMap<>();

        CompletableFuture<Void> smhi = fetchAndProcessWeatherData("SMHI",
                smhiApi.fetchWeatherSmhiAsync(city));
        CompletableFuture<Void> yr = fetchAndProcessWeatherData("YR",
                yrApi.fetchWeatherYrAsync(city));
        CompletableFuture.allOf(smhi,yr).join();

        if (mergedWeatherData.isEmpty()) {
            throw new WeatherNotFilledException("Could not connect to any weather API");
        }

        calculateAverages();
        setScaleWeatherData();

        Weather mergedWeather = Weather.builder()
                .message("Merged weather for " + city.getName() + " from SMHI, YR and FMI")
                .weatherData(mergedWeatherData)
                .timestamp(ZonedDateTime.now(ZoneId.of("UTC")))
                .city(city)
                .build();

        mergedWeather.getWeatherData().entrySet().removeIf(entry -> entry.getValue().getWeatherCode() == -1);
        cacheDB.save(mergedWeather, true, true);
        getSunriseSunset(mergedWeather);
        Objects.requireNonNull(cacheManager.getCache(cacheName)).put(key, mergedWeather);
        return mergedWeather;
    }

    private CompletableFuture<Void> fetchAndProcessWeatherData(String apiName, CompletableFuture<Weather> weatherFuture) {
        return weatherFuture
                .exceptionally(e -> {
                    log.error("Failed to fetch weather data from {}", apiName, e);
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

                existingData.setTemperature(existingData.getTemperature() + newDataItem.getTemperature());
                existingData.setWindSpeed(existingData.getWindSpeed() + newDataItem.getWindSpeed());
                existingData.setPrecipitation(existingData.getPrecipitation() + newDataItem.getPrecipitation());

                int weatherCode;
                if (api.equals("SMHI")) {
                    weatherCode = newDataItem.getWeatherCode();
                } else {
                    weatherCode = existingData.getWeatherCode() > -1 ? existingData.getWeatherCode() : newDataItem.getWeatherCode();
                }
                existingData.setWeatherCode(weatherCode);

                // Calculate average wind direction using vector addition of Cartesian coordinates
                existingData.setWindDirection(getAvgWindDirection(existingData, newDataItem));

            } else {
                mergedWeatherData.put(key, newDataItem);
            }
        }
        mergeCount++;
        log.info("Merged weather data from {}", api);
    }

    private void calculateAverages() {
        for (Weather.WeatherData data : mergedWeatherData.values()) {
            data.setTemperature(data.getTemperature() / mergeCount);
            data.setWindSpeed(data.getWindSpeed() / mergeCount);
            data.setPrecipitation(data.getPrecipitation() / mergeCount);
        }
    }

    private float getAvgWindDirection(Weather.WeatherData existingData, Weather.WeatherData newDataItem) {
        // Convert wind directions to Cartesian coordinates
        double existingX = Math.cos(Math.toRadians(existingData.getWindDirection()));
        double existingY = Math.sin(Math.toRadians(existingData.getWindDirection()));
        double newX = Math.cos(Math.toRadians(newDataItem.getWindDirection()));
        double newY = Math.sin(Math.toRadians(newDataItem.getWindDirection()));

        // Sum up Cartesian coordinates
        double sumX = existingX + newX;
        double sumY = existingY + newY;

        // Convert back to polar coordinates
        float avgWindDirection = (float) Math.toDegrees(Math.atan2(sumY, sumX));
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