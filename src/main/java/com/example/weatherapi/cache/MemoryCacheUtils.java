package com.example.weatherapi.cache;

import com.example.weatherapi.domain.weather.Weather;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class MemoryCacheUtils {

    private final Logger log;
    CaffeineCache cache;

    @Value("${cache.time.in.minutes}")
    private int cacheTimeInMinutes;

    @Autowired
    public MemoryCacheUtils(CacheManager cacheManager) {
        String cacheName = "cache";
        this.log = LoggerFactory.getLogger(MemoryCacheUtils.class);
        this.cache = (CaffeineCache) Objects.requireNonNull(cacheManager.getCache(cacheName));
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

        ZonedDateTime cacheExpiryTime = ZonedDateTime.now(ZoneId.of("UTC")).minusMinutes(cacheTimeInMinutes);
        ZonedDateTime timestamp = weatherFromCache.getTimestamp();

        if(timestamp.isBefore(cacheExpiryTime)){
            logCacheMessage(cityName, enabledApis, timestamp.truncatedTo(ChronoUnit.SECONDS));
            return null;
        }

        logCacheMessage(cityName, enabledApis, timestamp.truncatedTo(ChronoUnit.SECONDS));
        return weatherFromCache;

    }

    public void logCacheMessage(String cityName, List<String> enabledApis, ZonedDateTime timestamp) {
        if (enabledApis != null && !enabledApis.isEmpty()) {
            log.info("Cache hit for City: {} with Custom APIs: {} Timestamp: {}", cityName, enabledApis, timestamp);
        } else {
            log.info("Cache hit for City: {} Timestamp: {}", cityName, timestamp);
        }
    }

    public void putWeatherInCache(String key, Weather weather) {
        cache.put(key, weather);
    }

    public void evictCacheIfPresent(String key, String cityName) {
        if (cache.getNativeCache().asMap().containsKey(key)) {
            log.info("API statuses have changed, invalidating in-memory cache for {}", cityName);
            cache.evict(key);
        }
    }

}
