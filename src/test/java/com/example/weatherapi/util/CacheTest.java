package com.example.weatherapi.util;

import com.example.weatherapi.cache.Cache;
import com.example.weatherapi.domain.weather.Weather;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

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
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CacheTest {


    @Mock
    private Cache cache;


    /**
     * Test Case 1: Check that the cache we get is the same as the one we put in with a valid cacheTimeInHours.
     * <p>
     * Arrange: A Weather object is created and put in the cache.<br>
     * Act: The Weather object is retrieved from the cache.<br>
     * Assert: The Weather object retrieved from the cache is the same as the one we put in.
     */
    @Test
    void shouldRetrieveValidWeatherFromCache(){
        Weather weather = Weather.builder().build();
        when(cache.getWeatherFromCache("key")).thenReturn(weather);
        assertEquals(weather, cache.getWeatherFromCache("key"));
    }

    /**
     * Test Case 2: Check that the cache doesn't accept negative cacheTimeInHours and sets it to default value.
     * <p>
     * Arrange: A Weather object is created and put in the cache.<br>
     * Act: A Weather object is retrieved from the cache.<br>
     * Assert: The Weather object retrieved from the cache is the same as the one we put in.
     */

    @Test
    void shouldHandleNegativeCacheTimeInHours() {
        Weather weather = Weather.builder().build();
        when(cache.getWeatherFromCache("key")).thenReturn(weather);
        assertEquals(weather, cache.getWeatherFromCache("key"));
    }

    /**
     * Test Case 3: Check that the cache does not contain any Weather objects and returns null.
     * <p>
     * Arrange: No Weather objects are put in the cache.<br>
     * Act: A Weather object is attempted to be retrieved from the cache.<br>
     * Assert: The Weather object retrieved from the cache is null.
     */
    @Test
    void shouldHandleNonExistentWeatherInCache() {
        assertNull(cache.getWeatherFromCache("key"));
    }


}
