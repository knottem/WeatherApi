package com.example.weatherapi.util;

import com.example.weatherapi.cache.ApiStatusCache;
import com.example.weatherapi.domain.entities.ApiStatus;
import com.example.weatherapi.exceptions.ApiDisabledException;
import com.example.weatherapi.exceptions.InvalidApiUsageException;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for validating weather data freshness and requested weather APIs.
 *
 * @author Erik Wallenius
 * @see <a href="https://github.com/knottem/WeatherApi">Repository Link</a>
 */
public class WeatherValidation {

    private WeatherValidation(){
        throw new IllegalStateException("Utility class");
    }

    /**
     * Checks whether weather data is still considered valid based on its timestamp
     * and the specified validity period.
     * <p>
     * The supplied clock is used to determine the current time, allowing the
     * validation logic to be tested with a fixed or custom clock.
     *
     * @param timeStamp the timestamp of the weather data
     * @param minutes the number of minutes for which the weather data remains valid
     * @param clock the clock used to determine the current time
     * @return {@code true} if the weather data is newer than the validity threshold,
     *         otherwise {@code false}
     */
    public static boolean isWeatherValid(ZonedDateTime timeStamp, int minutes, Clock clock) {
        return ZonedDateTime.now(clock).minusMinutes(minutes).isBefore(timeStamp);
    }

    /**
     * Checks whether weather data is still considered valid based on its timestamp
     * and the specified validity period.
     * <p>
     * The current UTC time is used when determining whether the data is valid.
     *
     * @param timeStamp the timestamp of the weather data
     * @param minutes the number of minutes for which the weather data remains valid
     * @return {@code true} if the weather data is newer than the validity threshold,
     *         otherwise {@code false}
     */
    public static boolean isWeatherValid(ZonedDateTime timeStamp, int minutes) {
        return isWeatherValid(timeStamp, minutes, Clock.systemUTC());
    }

    /**
     * Validates the provided APIs and returns the list of active APIs.
     *
     * @param enabledApis         the APIs enabled by the user
     * @param apiStatusCache the cache of API statuses
     * @return a list of active API names
     */
    public static List<String> validateApis(List<String> enabledApis, ApiStatusCache apiStatusCache) {

        List<ApiStatus> apis = apiStatusCache.getAllApiStatuses();

        Set<String> inactiveApiNames = new HashSet<>();
        Set<String> allValidApis = new HashSet<>();
        for (ApiStatus apiStatus : apis) {
            allValidApis.add(apiStatus.getApiName());
            if (!apiStatus.isActive()) {
                inactiveApiNames.add(apiStatus.getApiName());
            }
        }

        validateInvalidApis(enabledApis, allValidApis);
        validateTurnedOffApis(enabledApis, inactiveApiNames);
        validateFmiApi(enabledApis);

        return apis.stream()
                .filter(ApiStatus::isActive)
                .map(ApiStatus::getApiName)
                .toList();
    }


    /**
     * Validates that all enabled APIs are valid.
     *
     * @param enabledApis   the APIs enabled by the user
     * @param validApiNames the set of all valid API names
     * @throws InvalidApiUsageException if any invalid APIs are found
     */
    private static void validateInvalidApis(List<String> enabledApis, Set<String> validApiNames) {
        List<String> invalidApis = enabledApis.stream()
                .filter(api -> !validApiNames.contains(api))
                .toList();

        if (!invalidApis.isEmpty()) {
            throw new InvalidApiUsageException(
                    "Invalid API(s) detected (" + invalidApis.size() + "): " + String.join(", ", invalidApis));
        }
    }

    /**
     * Validates that no inactive APIs are used.
     *
     * @param enabledApis      the APIs enabled by the user
     * @param inactiveApiNames the set of inactive API names
     * @throws ApiDisabledException if any inactive APIs are found
     */
    private static void validateTurnedOffApis(List<String> enabledApis, Set<String> inactiveApiNames) {
        List<String> turnedOffApis = enabledApis.stream()
                .filter(inactiveApiNames::contains)
                .toList();

        if (!turnedOffApis.isEmpty()) {
            throw new ApiDisabledException("API(s) currently turned off: " + String.join(", ", turnedOffApis));
        }
    }

    /**
     * Validates that the FMI API is not used alone.
     *
     * @param enabledApis the APIs enabled by the user
     * @throws InvalidApiUsageException if FMI is the only enabled API
     */
    private static void validateFmiApi(List<String> enabledApis) {
        if (enabledApis.size() == 1 && enabledApis.contains("FMI")) {
            throw new InvalidApiUsageException("FMI API cannot be used alone");
        }
    }
}
