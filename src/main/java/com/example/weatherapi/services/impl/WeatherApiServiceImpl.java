package com.example.weatherapi.services.impl;

import com.example.weatherapi.cache.CacheDB;
import com.example.weatherapi.cache.MemoryCacheUtils;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeatherApiServiceImpl implements WeatherApiService {

    private final ApiStatusRepository apiStatusRepository;
    private final MemoryCacheUtils memoryCacheUtils;
    private final CacheDB cacheDB;
    private final ObjectMapper objectMapper;
    private final Logger LOG;

    @Autowired
    public WeatherApiServiceImpl(
            ApiStatusRepository apiStatusRepository,
            MemoryCacheUtils memoryCacheUtils,
            CacheDB cacheDB) {
        this.apiStatusRepository = apiStatusRepository;
        this.memoryCacheUtils = memoryCacheUtils;
        this.cacheDB = cacheDB;
        this.LOG = LoggerFactory.getLogger(WeatherApiServiceImpl.class);
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    @Transactional
    public Weather fetchWeatherData(String apiName, City city, boolean smhiFlag, boolean yrFlag, boolean fmiFlag, boolean validateApiStatus) {
        String apiUpper = apiName.toUpperCase();
        String key = getKey(city, apiUpper);

        Weather weatherFromCache = memoryCacheUtils.getWeatherFromCache(key, city.getName(), smhiFlag, yrFlag, fmiFlag);
        if (weatherFromCache != null) {
            return weatherFromCache;
        }

        if (validateApiStatus) {
            ApiStatus apiStatus = apiStatusRepository.findByApiName(apiUpper);
            if (apiStatus == null || !apiStatus.isActive()) {
                LOG.warn("{} API is currently inactive", apiUpper);
                throw new ApiDisabledException(apiUpper + " API is currently inactive");
            }
        }

        return null;
    }

    @Override
    @Transactional
    public void saveWeatherData(String apiName, Weather weather, boolean smhiFlag, boolean yrFlag, boolean fmiFlag) {
        String key = getKey(weather.getCity(), apiName);
        cacheDB.save(weather, smhiFlag, yrFlag, fmiFlag);
        memoryCacheUtils.putWeatherInCache(key, objectMapper.convertValue(weather, Weather.class));
    }

    private String getKey(City city, String apiName) {
        return city.getName().toLowerCase() + apiName.toUpperCase();
    }

}
