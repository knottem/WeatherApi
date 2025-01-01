package com.example.weatherapi.cache;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.entities.ApiStatus;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.repositories.ApiStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.example.weatherapi.util.SunriseUtil.getSunriseSunset;
import static com.example.weatherapi.util.WeatherValidation.isWeatherValid;

@Service
public class WeatherCacheCoordinator {

    private final CacheDB cacheDB;
    private final MemoryCacheUtils memoryCacheUtils;
    private final Logger log;
    private final ApiStatusRepository apiStatusRepository;

    @Value("${cache.time.in.minutes}")
    private int cacheTimeInMinutes;


    @Autowired
    public WeatherCacheCoordinator(CacheDB cacheDB,
                                   MemoryCacheUtils memoryCacheUtils,
                                   ApiStatusRepository apiStatusRepository) {
        this.cacheDB = cacheDB;
        this.memoryCacheUtils = memoryCacheUtils;
        this.log = LoggerFactory.getLogger(WeatherCacheCoordinator.class);
        this.apiStatusRepository = apiStatusRepository;
    }

    public Optional<Weather> checkForMergedWeatherData(City city, String cacheKey) {
        Map<String, Boolean> apiStatusMap = getApiStatus();

        Weather weather = cacheDB.getWeatherFromCache(
                city.getName(),
                apiStatusMap.getOrDefault("SMHI", false),
                apiStatusMap.getOrDefault("YR", false),
                apiStatusMap.getOrDefault("FMI", false)
        );

        if (weather == null) {
            return Optional.empty();
        }

        if(!isWeatherValid(weather.getTimestamp(), cacheTimeInMinutes)){
            log.info("Cached weather data for City: {} is stale (timestamp: {}). Ignoring cached data.",
                    city.getName(),
                    weather.getTimestamp()
            );
            return Optional.empty();
        }

        // Update in-memory cache
        getSunriseSunset(weather);
        memoryCacheUtils.putWeatherInCache(cacheKey, weather);
        return Optional.of(weather);
    }


    public Map<String, Boolean> getApiStatus() {
        Map<String, Boolean> apiStatusMap = new HashMap<>();
        List<ApiStatus> apiStatusList = apiStatusRepository.findAll();
        for (ApiStatus apiStatus : apiStatusList) {
            apiStatusMap.put(apiStatus.getApiName(), apiStatus.isActive());
        }
        return apiStatusMap;
    }



}