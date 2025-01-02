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

import java.util.Collections;
import java.util.List;


@Service
public class WeatherApiServiceImpl implements WeatherApiService {

    private static final Logger LOG = LoggerFactory.getLogger(WeatherApiServiceImpl.class);

    private final ApiStatusRepository apiStatusRepository;
    private final MemoryCacheUtils memoryCacheUtils;
    private final CacheDB cacheDB;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    public WeatherApiServiceImpl(
            ApiStatusRepository apiStatusRepository,
            MemoryCacheUtils memoryCacheUtils,
            CacheDB cacheDB) {
        this.apiStatusRepository = apiStatusRepository;
        this.memoryCacheUtils = memoryCacheUtils;
        this.cacheDB = cacheDB;
    }

    @Override
    @Transactional
    public Weather fetchWeatherData(String apiName, City city, boolean smhiFlag, boolean yrFlag, boolean fmiFlag) {
        String apiUpper = apiName.toUpperCase();
        String key = getKey(city, apiUpper);

        Weather weatherFromCache = memoryCacheUtils.getWeatherFromCache(key, city.getName(), smhiFlag, yrFlag, fmiFlag);
        if (weatherFromCache != null) {
            return weatherFromCache;
        }

        Weather weatherFromCacheDB = cacheDB.getWeatherFromCache(city.getName(), smhiFlag, yrFlag, fmiFlag);
        if (weatherFromCacheDB != null) {
            LOG.debug("Weather data for {} fetched from cacheDB", city.getName());
            memoryCacheUtils.putWeatherInCache(key, weatherFromCacheDB);
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
        String apiUpper = apiName.toUpperCase();
        String key = getKey(city, apiUpper);
        List<String> enabledApis = Collections.singletonList(apiUpper);
        return memoryCacheUtils.getWeatherFromCache(key, city.getName(), enabledApis);
    }

    @Override
    @Transactional
    public void saveWeatherData(String apiName, Weather weather, boolean smhiFlag, boolean yrFlag, boolean fmiFlag) {
        String key = getKey(weather.getCity(), apiName);
        cacheDB.save(weather, smhiFlag, yrFlag, fmiFlag);
        Weather weatherCopy;
        weatherCopy = objectMapper.convertValue(weather, Weather.class);
        memoryCacheUtils.putWeatherInCache(key, weatherCopy);
    }

    private String getKey(City city, String apiName) {
        return city.getName().toLowerCase() + apiName.toUpperCase();
    }

}
