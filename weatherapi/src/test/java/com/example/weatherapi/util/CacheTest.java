package com.example.weatherapi.util;

import com.example.weatherapi.domain.weather.Weather;
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

    @Test
    public void getWeatherFromCacheTest_Valid(){
        Weather weather = Weather.builder().build();
        Cache.getInstance().put("key", weather);
        assertEquals(weather, Cache.getInstance().getWeatherFromCache("key", 1));
    }

    @Test
    public void getWeatherFromCacheTest_Expired(){
        Weather weather = Weather.builder().build();
        Cache.getInstance().put("key", weather);
        assertNull(Cache.getInstance().getWeatherFromCache("key", -1));
    }

    @Test
    public void getWeatherFromCacheTest_DoesntExist() {
        assertNull(Cache.getInstance().getWeatherFromCache("key", 1));
    }

    @Test
    public void putTest(){
        Weather weather = Weather.builder().build();
        Cache.getInstance().put("key", weather);
        assertEquals(weather, Cache.getInstance().getWeatherFromCache("key", 1));
    }

}
