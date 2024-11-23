package com.example.weatherapi.util;

import com.example.weatherapi.domain.entities.ApiStatus;
import com.example.weatherapi.exceptions.ApiDisabledException;
import com.example.weatherapi.exceptions.InvalidApiUsageException;
import com.example.weatherapi.repositories.ApiStatusRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WeatherValidation {

    private WeatherValidation(){
        throw new IllegalStateException("Utility class");
    }

    private static final Logger log = LoggerFactory.getLogger(WeatherValidation.class);

    private static final Cache<String, List<ApiStatus>> apiStatusCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public static boolean isWeatherValid(ZonedDateTime timeStamp, int minutes, Clock clock) {
        return ZonedDateTime.now(clock).minusMinutes(minutes).isBefore(timeStamp);
    }

    public static boolean isWeatherValid(ZonedDateTime timeStamp, int minutes) {
        return isWeatherValid(timeStamp, minutes, Clock.systemUTC());
    }

    public static List<String> validateApis(List<String> enabledApis, ApiStatusRepository apiStatusRepository) {

        List<ApiStatus> apis = apiStatusCache.get("apiStatus", key -> {
            log.debug("Fetching API status from the database");
            return apiStatusRepository.findAll();
        });

        // Get all valid API names and inactive API names from the database
        Set<String> validApiNames = new HashSet<>();
        Set<String> inactiveApiNames = new HashSet<>();

        for (ApiStatus apiStatus : apis) {
            validApiNames.add(apiStatus.getApiName());
            if (!apiStatus.isActive()) {
                inactiveApiNames.add(apiStatus.getApiName());
            }
        }

        List<String> invalidApis = enabledApis.stream()
                .filter(api -> !validApiNames.contains(api))
                .toList();

        if (!invalidApis.isEmpty()) {
            throw new InvalidApiUsageException("Invalid API(s): " + String.join(", ", invalidApis));
        }

        List<String> turnedOffApis = enabledApis.stream()
                .filter(inactiveApiNames::contains)
                .toList();

        if (!turnedOffApis.isEmpty()) {
            throw new ApiDisabledException("API(s) currently turned off: " + String.join(", ", turnedOffApis));
        }

        // Check if FMI API is used alone, which is not allowed currently
        if (enabledApis.size() == 1 && enabledApis.contains("FMI")) {
            throw new InvalidApiUsageException("FMI API cannot be used alone");
        }

        return apis.stream()
                .filter(ApiStatus::isActive)
                .map(ApiStatus::getApiName)
                .toList();
    }
}
