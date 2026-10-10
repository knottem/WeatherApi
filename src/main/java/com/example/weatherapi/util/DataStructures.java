package com.example.weatherapi.util;

import com.example.weatherapi.domain.weather.Weather;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Holds the data structures used while fetching and merging weather data
 * from multiple weather APIs.
 * <p>
 * The record stores the merged weather data, keeps track of how many times
 * individual weather attributes have been updated for each timestamp, and
 * records which APIs successfully returned weather data.
 *
 * @param mergedWeatherData the weather data merged from the selected APIs, grouped by timestamp
 * @param updateCountMap tracks the number of updates for each weather attribute and timestamp when calculating averages
 * @param successfulApis the names of APIs that successfully returned weather data
 *
 * @author Erik Wallenius
 * @see <a href="https://github.com/knottem/WeatherApi">Repository Link</a>
 */
public record DataStructures(
        Map<ZonedDateTime, Weather.WeatherData> mergedWeatherData,
        Map<String, Map<ZonedDateTime, Map<String, Integer>>> updateCountMap,
        List<String> successfulApis
) {

    /**
     * Creates an empty set of data structures used for merging weather data.
     */
    public DataStructures() {
        this(new TreeMap<>(), new ConcurrentHashMap<>(), Collections.synchronizedList(new ArrayList<>()));
    }
}
