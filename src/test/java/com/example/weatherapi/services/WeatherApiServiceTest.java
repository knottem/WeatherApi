package com.example.weatherapi.services;

import com.example.weatherapi.cache.CacheDB;
import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.exceptions.ApiDisabledException;
import com.example.weatherapi.repositories.ApiStatusRepository;
import com.example.weatherapi.services.impl.WeatherApiServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WeatherApiServiceTest {

    @InjectMocks
    private WeatherApiServiceImpl weatherApiService;

    @Mock
    private ApiStatusRepository apiStatusRepository;

    @Mock
    private CacheDB cacheDB;

    @Mock
    private CacheManager cacheManager;

    private City testCity;
    private Weather testWeather;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize test data
        testCity = new City();
        testCity.setName("TestCity");

        testWeather = Weather.builder()
                .city(testCity)
                .build();

        // Set up cacheManager mock to return a mock cache
        ConcurrentMapCache mockCache = new ConcurrentMapCache("cache");
        when(cacheManager.getCache("cache")).thenReturn(mockCache);
    }

    @Test
    void fetchWeatherDataFromCache() {
        // Put weather data into cache
        Objects.requireNonNull(cacheManager.getCache("cache")).put("testcitysmhi", testWeather);

        Weather weather = weatherApiService.fetchWeatherData("SMHI", testCity, true, false, false);

        assertThat(weather).isNotNull();
        assertThat(weather.getCity()).isEqualTo(testWeather.getCity());
        verify(cacheDB, never()).getWeatherFromCache(anyString(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    void fetchWeatherDataFromDatabase() {
        // Mock database response
        when(cacheDB.getWeatherFromCache("TestCity", true, false, false)).thenReturn(testWeather);

        Weather weather = weatherApiService.fetchWeatherData("SMHI", testCity, true, false, false);

        assertThat(weather).isNotNull();
        assertThat(weather.getCity()).isEqualTo(testWeather.getCity());
        verify(cacheDB, times(1)).getWeatherFromCache("TestCity", true, false, false);
        assertThat(cacheManager.getCache("cache").get("testcitysmhi", Weather.class)).isEqualTo(testWeather);
    }

    @Test
    void fetchWeatherDataApiDisabled() {
        // Mock API status as disabled
        when(apiStatusRepository.findByApiName("SMHI")).thenThrow(
                new ApiDisabledException("SMHI API is currently inactive"));

        ApiDisabledException exception = assertThrows(ApiDisabledException.class, () ->
                weatherApiService.fetchWeatherData("SMHI", testCity, true, false, false));

        assertThat(exception.getMessage()).isEqualTo("SMHI API is currently inactive");
    }

    @Test
    void saveWeatherData() {
        weatherApiService.saveWeatherData("SMHI", testWeather, true, false, false);

        verify(cacheDB, times(1)).save(testWeather, true, false, false);
        assertThat(cacheManager.getCache("cache").get("testcitysmhi", Weather.class)).isEqualTo(testWeather);
    }


}