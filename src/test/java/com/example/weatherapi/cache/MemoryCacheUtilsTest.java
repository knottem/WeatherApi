package com.example.weatherapi.cache;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.Weather;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MemoryCacheUtilsTest {

    @Autowired
    private MemoryCacheUtils memoryCacheUtils;

    private Weather testWeather;
    private final String testKey = "testcitySMHI";

    @BeforeEach
    void setUp() {

        // Test data setup
        City testCity = new City();
        testCity.setName("TestCity");

        testWeather = Weather.builder()
                .city(testCity)
                .timestamp(ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(30))
                .build();

    }

    @AfterEach
    void tearDown() {
        memoryCacheUtils.evictCacheIfPresent(testKey, "TestCity");
    }

    @Test
    void testGetWeatherFromCache_Valid() {

        memoryCacheUtils.putWeatherInCache(testKey, testWeather);

        Weather cachedWeather = memoryCacheUtils.getWeatherFromCache(testKey, "TestCity", Collections.emptyList());
        System.out.println("Cached weather retrieved from MemoryCacheUtils: " + cachedWeather);

        assertNotNull(cachedWeather, "Cached weather should not be null");
        assertEquals(testWeather.getCity().getName(), cachedWeather.getCity().getName(), "City names should match");
        assertEquals(testWeather.getTimestamp(), cachedWeather.getTimestamp(), "Timestamps should match");
    }

    @Test
    void testEvictCacheIfPresent() {
        memoryCacheUtils.putWeatherInCache(testKey, testWeather);
        Weather cachedWeather = memoryCacheUtils.getWeatherFromCache(testKey, "TestCity", Collections.emptyList());
        assertNotNull(cachedWeather, "Weather should be present in the cache before eviction");
        memoryCacheUtils.evictCacheIfPresent(testKey, "TestCity");
        cachedWeather = memoryCacheUtils.getWeatherFromCache(testKey, "TestCity", Collections.emptyList());
        assertNull(cachedWeather, "Weather should not be present in the cache after eviction");
    }

    @Test
    void testGetWeatherFromCache_NonExistentKey() {
        Weather cachedWeather = memoryCacheUtils.getWeatherFromCache("nonExistentKey", "TestCity", Collections.emptyList());
        assertNull(cachedWeather, "Cache should return null for a non-existent key");
    }

    @Test
    void testDeepCopyBehavior() {
        memoryCacheUtils.putWeatherInCache("testKey", testWeather);

        Weather cachedWeather = memoryCacheUtils.getWeatherFromCache("testKey", "TestCity", Collections.emptyList());

        assertNotSame(testWeather, cachedWeather);

        assertEquals(testWeather.getCity().getName(), cachedWeather.getCity().getName());
        assertEquals(testWeather.getTimestamp(), cachedWeather.getTimestamp());
    }

}