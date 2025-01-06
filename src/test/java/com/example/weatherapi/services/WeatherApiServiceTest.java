package com.example.weatherapi.services;

import com.example.weatherapi.cache.CacheDB;
import com.example.weatherapi.cache.MemoryCacheUtils;
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
    private MemoryCacheUtils memoryCacheUtils;

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

    }

    @Test
    void fetchWeatherDataFromCache() {
        when(memoryCacheUtils.getWeatherFromCache(anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(testWeather);

        Weather weather = weatherApiService.fetchWeatherData("SMHI", testCity, true, false, false, false);

        assertThat(weather).isNotNull();
        assertThat(weather.getCity()).isEqualTo(testWeather.getCity());
        verify(cacheDB, never()).getWeatherFromCache(anyString(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    void fetchWeatherDataApiDisabled() {
        // Mock API status as disabled
        when(apiStatusRepository.findByApiName("SMHI")).thenThrow(
                new ApiDisabledException("SMHI API is currently inactive"));

        ApiDisabledException exception = assertThrows(ApiDisabledException.class, () ->
                weatherApiService.fetchWeatherData("SMHI", testCity, true, false, false, true));

        assertThat(exception.getMessage()).isEqualTo("SMHI API is currently inactive");
    }

    @Test
    void saveWeatherData() {
        when(memoryCacheUtils.getWeatherFromCache(anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(testWeather);

        weatherApiService.saveWeatherData("SMHI", testWeather, true, false, false);
        verify(cacheDB, times(1)).saveDB(testWeather, true, false, false);
        assertThat(memoryCacheUtils.getWeatherFromCache("TestcitySMHI", "TestCity", true, false, false)).isEqualTo(testWeather);
    }


}