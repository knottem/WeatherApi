package com.example.weatherapi.util;

import com.example.weatherapi.domain.weather.Weather;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CacheTest {

    // Clear the cache before each test
    @BeforeEach
    public void tearDown(){
        Cache.getInstance().clear();
    }

    // Test Case 1: Check that the cache we get is the same as the one we put in
    @Test
    public void shouldRetrieveValidWeatherFromCache(){
        Weather weather = Weather.builder().build();
        Cache.getInstance().put("key", weather);
        assertEquals(weather, Cache.getInstance().getWeatherFromCache("key", 1));
    }

    // Test Case 2: Check that the cache is expired
    @Test
    public void shouldDetectExpiredWeatherInCache(){
        Weather weather = Weather.builder().build();
        Cache.getInstance().put("key", weather);
        assertNull(Cache.getInstance().getWeatherFromCache("key", -1));
    }

    // Test Case 3: Check that the cache doesn't exist
    @Test
    public void shouldHandleNonExistentWeatherInCache() {
        assertNull(Cache.getInstance().getWeatherFromCache("key", 1));
    }
}
