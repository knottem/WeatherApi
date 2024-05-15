package com.example.weatherapi.services.impl;

import com.example.weatherapi.api.FmiApi;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.weatherapi.util.CityMapper.toModel;
import static com.example.weatherapi.util.SunriseUtil.getSunriseSunset;

@Service
public class WeatherServiceImpl implements WeatherService {

    private final CityService cityService;
    private final FmiApi fmiApi;
    private final SmhiApi smhiApi;
    private final YrApi yrApi;
    private final CacheDB cacheDB;
    private final Logger log;
    private Map<ZonedDateTime, Weather.WeatherData> mergedWeatherData;
    private Map<String, Map<ZonedDateTime, Map<String, Integer>>> updateCountMap;
    private final CacheManager cacheManager;
    private final String cacheName;
    private List<String> successfulApis;


    @Autowired
    public WeatherServiceImpl(CityService cityService, SmhiApi smhiApi, YrApi yrApi, FmiApi fmiApi, CacheDB cacheDB, CacheManager cacheManager) {
        this.cityService = cityService;
        this.fmiApi = fmiApi;
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
        //temp fmi false
        Weather weatherFromCacheDB = cacheDB
                .getWeatherFromCache(city.getName(), true, true, false);
        if(weatherFromCacheDB != null) {
            getSunriseSunset(weatherFromCacheDB);
            Objects.requireNonNull(cacheManager.getCache(cacheName))
                    .put(key, weatherFromCacheDB);
            return weatherFromCacheDB;
        }

        this.updateCountMap = new ConcurrentHashMap<>();
        this.mergedWeatherData = new TreeMap<>();
        this.successfulApis = Collections.synchronizedList(new ArrayList<>());

        CompletableFuture<Void> smhi = fetchAndProcessWeatherData("SMHI", smhiApi.fetchWeatherSmhiAsync(city));
        CompletableFuture<Void> yr = fetchAndProcessWeatherData("YR", yrApi.fetchWeatherYrAsync(city));

        // Wait for both SMHI and YR to finish before fetching FMI
        CompletableFuture.allOf(smhi,yr)
                .thenRun(() -> fetchAndProcessWeatherData("FMI", fmiApi.fetchWeatherFmiAsync(city)).join())
                .join();

        if (mergedWeatherData.isEmpty()) {
            throw new WeatherNotFilledException("Could not connect to any weather API");
        }

        calculateAverages();
        setScaleWeatherData();

        Weather mergedWeather = Weather.builder()
                .message(createMessage(city))
                .weatherData(mergedWeatherData)
                .timestamp(ZonedDateTime.now(ZoneId.of("UTC")))
                .city(city)
                .build();

        mergedWeather.getWeatherData().entrySet().removeIf(entry -> entry.getValue().getWeatherCode() == -1);
        // temp fmi false
        cacheDB.save(mergedWeather, true, true, false);
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
                        successfulApis.add(apiName);
                    }
                });
    }

    private synchronized void mergeWeatherDataIntoMergedData(Map<ZonedDateTime, Weather.WeatherData> newData, String api) {
        for (Map.Entry<ZonedDateTime, Weather.WeatherData> entry : newData.entrySet()) {
            ZonedDateTime key = entry.getKey();
            Weather.WeatherData newDataItem = entry.getValue();

            String precipitation = "precipitation";
            String temp = "temperature";
            String windSpeed = "windSpeed";
            if (mergedWeatherData.containsKey(key)) {
                Weather.WeatherData existingData = mergedWeatherData.get(key);

                if(newDataItem.getTemperature() != -99f) {
                    existingData.setTemperature(existingData.getTemperature() + newDataItem.getTemperature());
                    updateCount(key, temp);
                }

                if(newDataItem.getWindSpeed() != -99f) {
                    existingData.setWindSpeed(existingData.getWindSpeed() + newDataItem.getWindSpeed());
                    updateCount(key, windSpeed);
                }
                if(newDataItem.getPrecipitation() != -99f) {
                    existingData.setPrecipitation(existingData.getPrecipitation() + newDataItem.getPrecipitation());
                    updateCount(key, precipitation);
                }

                if(newDataItem.getWindDirection() != -99f) {
                    existingData.setWindDirection(getAvgWindDirection(
                            existingData.getWindDirection(),
                            newDataItem.getWindDirection()));
                }

                int weatherCode;
                if (api.equals("SMHI")) {
                    weatherCode = newDataItem.getWeatherCode();
                } else {
                    weatherCode = existingData.getWeatherCode() > -1 ? existingData.getWeatherCode() : newDataItem.getWeatherCode();
                }
                existingData.setWeatherCode(weatherCode);

            } else if (!api.equals("FMI")){
                // If the data is from FMI, we don't want to add it to the merged data if it doesn't already exist due to differences in timestamps compared to SMHI and YR
                mergedWeatherData.put(key, newDataItem);
                updateCount(key, temp);
                updateCount(key, windSpeed);
                updateCount(key, precipitation);
            }
        }

        log.info("Merged weather data from {}", api);
    }

    private void updateCount(ZonedDateTime key, String attribute) {
        updateCountMap.computeIfAbsent(attribute, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(key, k -> new ConcurrentHashMap<>())
                .merge(attribute, 1, Integer::sum);
    }

    private int getUpdateCount(ZonedDateTime key, String attribute) {
        return updateCountMap.getOrDefault(attribute, Collections.emptyMap())
                .getOrDefault(key, Collections.emptyMap())
                .getOrDefault(attribute, 1);
    }

    private void calculateAverages() {
        for (Map.Entry<ZonedDateTime, Weather.WeatherData> entry : mergedWeatherData.entrySet()) {
            ZonedDateTime key = entry.getKey();
            Weather.WeatherData data = entry.getValue();

            data.setTemperature(data.getTemperature() / getUpdateCount(key, "temperature"));
            data.setWindSpeed(data.getWindSpeed() / getUpdateCount(key, "windSpeed"));
            data.setPrecipitation(data.getPrecipitation() / getUpdateCount(key, "precipitation"));

        }
    }

    private float getAvgWindDirection(float existingWindDirection, float newDataWindDirection) {
        // Convert wind directions to Cartesian coordinates
        double existingX = Math.cos(Math.toRadians(existingWindDirection));
        double existingY = Math.sin(Math.toRadians(existingWindDirection));
        double newX = Math.cos(Math.toRadians(newDataWindDirection));
        double newY = Math.sin(Math.toRadians(newDataWindDirection));

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

    private String createMessage(City city){
        List<String> sortedApis = new ArrayList<>(successfulApis);
        Collections.sort(sortedApis);

        StringBuilder messageBuilder = new StringBuilder("Merged weather for ")
                .append(city.getName())
                .append(" from ");

        if (sortedApis.size() == 1) {
            messageBuilder.append(sortedApis.get(0));
        } else {
            messageBuilder.append(String.join(", ", sortedApis.subList(0, sortedApis.size() - 1)))
                    .append(" and ")
                    .append(sortedApis.get(sortedApis.size() - 1));
        }

        return messageBuilder.toString();
    }

}