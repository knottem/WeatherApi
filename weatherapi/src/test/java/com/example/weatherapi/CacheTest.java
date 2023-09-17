package com.example.weatherapi;

import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.util.Cache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CacheTest {

    @AfterEach
    public void tearDown(){
        Cache.getInstance().clear();
    }

    // Test Case 1: Check that the cache we get is the same as the one we put in
    @Test
    public void getWeatherFromCacheTest_Valid(){
        Weather weather = Weather.builder().build();
        Cache.getInstance().put("key", weather);
        assertEquals(weather, Cache.getInstance().getWeatherFromCache("key", 1));
    }

    // Test Case 2: Check that the cache is expired
    @Test
    public void getWeatherFromCacheTest_Expired(){
        Weather weather = Weather.builder().build();
        Cache.getInstance().put("key", weather);
        assertNull(Cache.getInstance().getWeatherFromCache("key", -1));
    }

    // Test Case 3: Check that the cache doesn't exist
    @Test
    public void getWeatherFromCacheTest_DoesntExist() {
        assertNull(Cache.getInstance().getWeatherFromCache("key", 1));
    }
}
