package com.example.weatherapi.util;

import com.example.weatherapi.domain.weather.Weather;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
* This class contains tests for the Cache class.
* We are checking that the cache works as expected and the cacheTimeInHours is respected.
*
* <p>
* Each test is annotated with {@code @Test}, which lets JUnit know to run the method as a test case.
*
*  @author Erik Wallenius
*  @see <a href="https://github.com/knottem/WeatherApi">Repository Link</a>
*/
public class CacheTest {


    /**
     * This method is run before each test case to clear the cache.
     */
    @BeforeEach
    public void tearDown(){
        Cache.getInstance().clear();
    }

    /**
     * Test Case 1: Check that the cache we get is the same as the one we put in with a valid cacheTimeInHours.
     * <p>
     * Arrange: A Weather object is created and put in the cache.<br>
     * Act: The Weather object is retrieved from the cache.<br>
     * Assert: The Weather object retrieved from the cache is the same as the one we put in.
     */
    @Test
    public void shouldRetrieveValidWeatherFromCache(){
        Weather weather = Weather.builder().build();
        Cache.getInstance().put("key", weather);
        assertEquals(weather, Cache.getInstance().getWeatherFromCache("key", 1));
    }

    /**
     * Test Case 2: Check that the cache is expired after the cacheTimeInHours has passed.
     * <p>
     * Arrange: A Weather object is created and put in the cache.<br>
     * Act: The Weather object is retrieved from the cache with a negative cacheTimeInHours.<br>
     * Assert: The Weather object retrieved from the cache is null.
     */
    @Test
    public void shouldDetectExpiredWeatherInCache(){
        Weather weather = Weather.builder().build();
        Cache.getInstance().put("key", weather);
        assertNull(Cache.getInstance().getWeatherFromCache("key", -1));
    }

    /**
     * Test Case 3: Check that the cache does not contain any Weather objects and returns null.
     * <p>
     * Arrange: The cache is cleared.<br>
     * Act: A Weather object is retrieved from the cache.<br>
     * Assert: The Weather object retrieved from the cache is null.
     */
    @Test
    public void shouldHandleNonExistentWeatherInCache() {
        assertNull(Cache.getInstance().getWeatherFromCache("key", 1));
    }
}
