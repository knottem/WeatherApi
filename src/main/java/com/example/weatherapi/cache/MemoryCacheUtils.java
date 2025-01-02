package com.example.weatherapi.cache;

import com.example.weatherapi.domain.weather.Weather;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class MemoryCacheUtils {

    private final Logger log;
    CaffeineCache cache;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    public MemoryCacheUtils(CacheManager cacheManager) {
        this.log = LoggerFactory.getLogger(MemoryCacheUtils.class);
        this.cache = (CaffeineCache) Objects.requireNonNull(cacheManager.getCache("cache"));
    }

    public Weather getWeatherFromCache(String key, String cityName, boolean smhi, boolean yr, boolean fmi){
        List<String> enabledApis = new ArrayList<>();
        if (smhi) enabledApis.add("SMHI");
        if (yr) enabledApis.add("YR");
        if (fmi) enabledApis.add("FMI");
        return getWeatherFromCache(key, cityName, enabledApis);
    }

    public Weather getWeatherFromCache(String key, String cityName, List<String> enabledApis) {
        Weather weatherFromCache = cache.get(key, Weather.class);

        if (weatherFromCache == null) {
            return null;
        }

        if (enabledApis != null && !enabledApis.isEmpty()) {
            log.info("Cache hit for City: {} with Custom APIs: {} Timestamp: {}", cityName, enabledApis, weatherFromCache.getTimestamp().truncatedTo(ChronoUnit.SECONDS));
        } else {
            log.info("Cache hit for City: {} Timestamp: {}", cityName, weatherFromCache.getTimestamp().truncatedTo(ChronoUnit.SECONDS));
        }

        return weatherFromCache;
    }

    public void putWeatherInCache(String key, Weather weather) {
        cache.put(key, objectMapper.convertValue(weather, Weather.class));
    }

    public void evictCacheIfPresent(String key, String cityName) {
        if (cache.getNativeCache().asMap().containsKey(key)) {
            log.info("Evicting cache for city: {}, key: {}", cityName, key);
            cache.evict(key);
        }
    }

}
