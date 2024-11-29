package com.example.weatherapi.util;

import com.example.weatherapi.domain.weather.Weather;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public record DataStructures(
        Map<ZonedDateTime, Weather.WeatherData> mergedWeatherData,
        Map<String, Map<ZonedDateTime, Map<String, Integer>>> updateCountMap,
        List<String> successfulApis
) {
    public DataStructures() {
        this(new TreeMap<>(), new ConcurrentHashMap<>(), Collections.synchronizedList(new ArrayList<>()));
    }
}
