package com.example.weatherapi.util;

import com.example.weatherapi.domain.weather.Weather;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Cache {

    private final Logger logger;
    private static Cache instance;
    private final Map<String, WeatherCache> cache;

    private Cache() {
        cache = new HashMap<>();
        logger = LoggerFactory.getLogger(Cache.class);
    }

    public static Cache getInstance() {
        if (instance == null) {
            instance = new Cache();
        }
        return instance;
    }

    public Weather getWeatherFromCache(String key, int cacheTimeInHours) {
        WeatherCache entry = cache.get(key);
        if(entry != null && entry.isValid(cacheTimeInHours)) {
            logger.info("Cache hit for key: " + key + ", returning cached data");
            return entry.getWeather();
        }  else if(entry != null) {
            logger.info("Cache expired for key: " + key + ", fetching new data");
        } else {
            logger.info("Cache doesn't exist for key: " + key + ", fetching new data");
        }
        return null;
    }

    public void put(String key, Weather weather) {
        cache.put(key, new WeatherCache(weather));
    }

    public void clear() {
        cache.clear();
    }

    static class WeatherCache {

        private final LocalDateTime timestamp;
        @Getter
        private final Weather weather;

        public WeatherCache(Weather weather) {
            this.weather = weather;
            this.timestamp = LocalDateTime.now();
        }

        public boolean isValid(int hours) {
            return LocalDateTime.now().minusHours(hours).isBefore(timestamp);
        }

    }

}
