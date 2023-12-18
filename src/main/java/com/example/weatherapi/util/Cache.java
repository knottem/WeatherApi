package com.example.weatherapi.util;

import com.example.weatherapi.domain.entities.WeatherCacheEntity;
import com.example.weatherapi.domain.entities.WeatherEntity;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.repositories.WeatherCacheRepository;
import com.example.weatherapi.repositories.WeatherDataRepository;
import com.example.weatherapi.repositories.WeatherEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.weatherapi.util.WeatherMapper.*;

@Service
public class Cache {

    private final Logger logger;

    private final WeatherCacheRepository weatherCacheRepository;
    private final WeatherEntityRepository weatherEntityRepository;
    private final WeatherDataRepository weatherDataRepository;

    @Value("${cache.time.in.hours}")
    private int CACHE_TIME_IN_HOURS;

    private Cache(WeatherCacheRepository weatherCacheRepository,
                  WeatherEntityRepository weatherEntityRepository,
                  WeatherDataRepository weatherDataRepository) {
        logger = LoggerFactory.getLogger(Cache.class);
        this.weatherCacheRepository = weatherCacheRepository;
        this.weatherEntityRepository = weatherEntityRepository;
        this.weatherDataRepository = weatherDataRepository;
    }

    public Weather getWeatherFromCache(String key) {
        if (CACHE_TIME_IN_HOURS < 0) {
            logger.warn("Cache time in hours is negative, setting it to default value of 3 hours");
            CACHE_TIME_IN_HOURS = 3;
        }

        Optional<WeatherCacheEntity> cachedWeatherOptional = weatherCacheRepository.findLatestByCacheKey(key);

        if (cachedWeatherOptional.isPresent()) {
            WeatherCacheEntity cachedWeather = cachedWeatherOptional.get();
            if (cachedWeather.isValid(CACHE_TIME_IN_HOURS)) {
                logger.info("Cache hit for key: " + key + ", returning cached data");
                return convertToWeather(cachedWeather.getWeather());
            } else {
                logger.info("Cache expired for key: " + key + ", fetching new data");
                //weatherCacheRepository.deleteByCacheKey(key);
            }
        } else {
            logger.info("Cache doesn't exist for key: " + key + ", fetching new data");
        }
        return null;
    }

    public void save(String key, Weather weather) {
        WeatherEntity weatherEntity = weatherEntityRepository.save(convertToWeatherEntity(weather));
        weatherDataRepository.saveAll(convertToWeatherDataEntity(weather.getWeatherData(), weatherEntity));
        WeatherCacheEntity weatherCacheEntity =
        weatherCacheRepository.save(WeatherCacheEntity.builder()
                .cacheKey(key)
                .timestamp(LocalDateTime.now())
                .weather(weatherEntity)
                .build());

        logger.info("Saved weather data to cache with key: " + key + ", id: " + weatherCacheEntity.getId());
    }

    public void clear() {
        weatherCacheRepository.deleteAll();
    }



}
