package com.example.weatherapi.services.impl;

import com.example.weatherapi.cache.CacheDB;
import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.entities.ApiStatus;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.exceptions.ApiDisabledException;
import com.example.weatherapi.repositories.ApiStatusRepository;
import com.example.weatherapi.services.WeatherApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

@Service
public class WeatherApiServiceImpl implements WeatherApiService {

    private static final Logger LOG = LoggerFactory.getLogger(WeatherApiServiceImpl.class);

    private final ApiStatusRepository apiStatusRepository;
    private final CacheDB cacheDB;
    private final CacheManager cacheManager;
    private final String cacheName;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    @Autowired
    public WeatherApiServiceImpl(ApiStatusRepository apiStatusRepository, CacheDB cacheDB, CacheManager cacheManager) {
        this.apiStatusRepository = apiStatusRepository;
        this.cacheDB = cacheDB;
        this.cacheManager = cacheManager;
        this.cacheName = "cache";
    }

    @Override
    @Transactional
    public Weather fetchWeatherData(String apiName, City city, boolean smhiFlag, boolean yrFlag, boolean fmiFlag) {
        String apiLower = apiName.toLowerCase();
        String apiUpper = apiName.toUpperCase();
        String key = city.getName().toLowerCase() + apiLower;
        Weather weatherFromCache = Objects.requireNonNull(cacheManager.getCache(cacheName))
                .get(key, Weather.class);
        if (weatherFromCache != null) {
            return objectMapper.convertValue(weatherFromCache, Weather.class);
        }

        Weather weatherFromCacheDB = cacheDB.getWeatherFromCache(city.getName(), smhiFlag, yrFlag, fmiFlag);
        if (weatherFromCacheDB != null) {
            Objects.requireNonNull(cacheManager.getCache(cacheName)).put(key, weatherFromCacheDB);
            return weatherFromCacheDB;
        }

        ApiStatus apiStatus = apiStatusRepository.findByApiName(apiUpper);
        if (apiStatus == null || !apiStatus.isActive()) {
            LOG.warn("{} API is currently inactive", apiUpper);
            throw new ApiDisabledException(apiUpper + " API is currently inactive");
        }

        return null;
    }

    @Override
    @Transactional
    public Weather fetchWeatherDataCached(String apiName, City city) {
        String apiLower = apiName.toLowerCase();
        String key = city.getName().toLowerCase() + apiLower;
        Weather weatherFromCache = Objects.requireNonNull(cacheManager.getCache(cacheName))
                .get(key, Weather.class);
        if (weatherFromCache != null) {
            LOG.info("Cache hit for City: {} in the cache, returning cached data for {}", city.getName(), apiLower);
            return weatherFromCache;
        }
        return null;
    }

    @Override
    @Transactional
    public void saveWeatherData(String apiName, Weather weather, boolean smhiFlag, boolean yrFlag, boolean fmiFlag) {
        String key = weather.getCity().getName().toLowerCase() + apiName.toLowerCase();
        cacheDB.save(weather, smhiFlag, yrFlag, fmiFlag);
        Weather weatherCopy;
        weatherCopy = objectMapper.convertValue(weather, Weather.class);
        Objects.requireNonNull(cacheManager.getCache(cacheName)).put(key, weatherCopy);

    }

}
