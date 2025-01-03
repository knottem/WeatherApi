package com.example.weatherapi.cache;

import com.example.weatherapi.domain.entities.LatestWeatherApiEntity;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.repositories.LatestWeatherApiRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.example.weatherapi.util.SunriseUtil.getSunriseSunset;
import static com.example.weatherapi.util.WeatherMapper.convertToWeather;

@Service
public class WeatherCachePreload {

    private final Logger log;
    private final LatestWeatherApiRepository latestWeatherApiRepository;
    private final MemoryCacheUtils memoryCacheUtils;
    private final int cacheTimeInMinutes;

    @Autowired
    public WeatherCachePreload(LatestWeatherApiRepository latestWeatherApiRepository,
                               MemoryCacheUtils memoryCacheUtils,
                               @Value("${cache.time.in.minutes}") int cacheTimeInMinute) {
        this.latestWeatherApiRepository = latestWeatherApiRepository;
        this.memoryCacheUtils = memoryCacheUtils;
        this.cacheTimeInMinutes = cacheTimeInMinute;
        this.log = LoggerFactory.getLogger(WeatherCachePreload.class);
    }

    @PostConstruct
    public void preloadValidWeatherData() {
        ZonedDateTime validThreshold = ZonedDateTime.now(ZoneId.of("UTC")).minusMinutes(cacheTimeInMinutes);

        List<LatestWeatherApiEntity> validWeatherData = latestWeatherApiRepository.findValidWeatherData(validThreshold);
        log.info("Preloading {} valid weather entries into memory cache", validWeatherData.size());

        for (LatestWeatherApiEntity entry : validWeatherData) {
            Weather weather = convertToWeather(entry.getLatestWeather());
            getSunriseSunset(weather);
            String cacheKey = determineCacheKey(entry);
            log.info("Preloading weather data for city: {} with cache key: {}", entry.getCity().getName(), cacheKey);
            memoryCacheUtils.putWeatherInCache(cacheKey, weather);
        }

    }

    public String determineCacheKey(LatestWeatherApiEntity entry) {
        String cityName = entry.getCity().getName().toLowerCase();

        List<String> apis = new ArrayList<>();
        if (entry.isSmhi()) apis.add("SMHI");
        if (entry.isYr()) apis.add("YR");
        if (entry.isFmi()) apis.add("FMI");

        apis = apis.stream().sorted().toList();

        if (apis.size() == 3) {
            return cityName + "merged";
        } else if (!apis.isEmpty()) {
            return cityName + String.join("_", apis);
        }

        throw new IllegalArgumentException("Invalid API combination for city: " + cityName);
    }

}