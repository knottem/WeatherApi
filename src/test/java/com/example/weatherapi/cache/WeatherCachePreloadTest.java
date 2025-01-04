package com.example.weatherapi.cache;

import com.example.weatherapi.domain.entities.CityEntity;
import com.example.weatherapi.domain.entities.LatestWeatherApiEntity;
import com.example.weatherapi.domain.entities.WeatherEntity;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.repositories.LatestWeatherApiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WeatherCachePreloadTest {

    private LatestWeatherApiRepository latestWeatherApiRepository;
    private MemoryCacheUtils memoryCacheUtils;
    private WeatherCachePreload weatherCachePreload;

    @BeforeEach
    void setUp() {
        latestWeatherApiRepository = Mockito.mock(LatestWeatherApiRepository.class);
        memoryCacheUtils = Mockito.mock(MemoryCacheUtils.class);
        weatherCachePreload = new WeatherCachePreload(latestWeatherApiRepository, memoryCacheUtils, 60);
    }

    @Test
    void testPreloadValidWeatherData_Success() {
        LatestWeatherApiEntity mockEntity = createMockEntity("TestCity", true, false, true);
        when(latestWeatherApiRepository.findValidWeatherData(any(ZonedDateTime.class)))
                .thenReturn(Collections.singletonList(mockEntity));

        weatherCachePreload.preloadValidWeatherData();

        verify(latestWeatherApiRepository, times(1)).findValidWeatherData(any(ZonedDateTime.class));
        verify(memoryCacheUtils, times(1)).putWeatherInCache(eq("testcityFMI_SMHI"), any(Weather.class));
    }

    @Test
    void testPreloadValidWeatherData_Multiple() {
        LatestWeatherApiEntity mockEntity1 = createMockEntity("TestCity1", true, false, true);
        LatestWeatherApiEntity mockEntity2 = createMockEntity("TestCity2", true, true, false);
        LatestWeatherApiEntity mockEntity3 = createMockEntity("TestCity3", true, true, true);
        when(latestWeatherApiRepository.findValidWeatherData(any(ZonedDateTime.class)))
                .thenReturn(List.of(mockEntity1, mockEntity2, mockEntity3));

        weatherCachePreload.preloadValidWeatherData();

        verify(latestWeatherApiRepository, times(1)).findValidWeatherData(any(ZonedDateTime.class));
        verify(memoryCacheUtils, times(3)).putWeatherInCache(anyString(), any(Weather.class));
        verify(memoryCacheUtils, times(1)).putWeatherInCache(eq("testcity1FMI_SMHI"), any(Weather.class));
        verify(memoryCacheUtils, times(1)).putWeatherInCache(eq("testcity2SMHI_YR"), any(Weather.class));
        verify(memoryCacheUtils, times(1)).putWeatherInCache(eq("testcity3merged"), any(Weather.class));
    }

    @Test
    void testPreloadValidWeatherData_Empty() {
        when(latestWeatherApiRepository.findValidWeatherData(any(ZonedDateTime.class)))
                .thenReturn(Collections.emptyList());

        weatherCachePreload.preloadValidWeatherData();

        verify(latestWeatherApiRepository, times(1)).findValidWeatherData(any(ZonedDateTime.class));
        verify(memoryCacheUtils, never()).putWeatherInCache(anyString(), any(Weather.class));
    }

    @Test
    void testDetermineCacheKey_CorrectKey() {
        LatestWeatherApiEntity mockEntity = createMockEntity("TestCity", false, true, true);
        assertEquals("testcityFMI_YR", weatherCachePreload.determineCacheKey(mockEntity));
    }

    @Test
    void testDetermineCacheKey_NullCity() {
        LatestWeatherApiEntity mockEntity = new LatestWeatherApiEntity();
        mockEntity.setCity(createMockCity("test"));
        assertThrows(IllegalArgumentException.class, () -> weatherCachePreload.determineCacheKey(mockEntity));
    }

    private LatestWeatherApiEntity createMockEntity(String cityName, boolean smhi, boolean yr, boolean fmi) {
        CityEntity mockCity = createMockCity(cityName);

        WeatherEntity mockWeather = new WeatherEntity();
        mockWeather.setCity(mockCity);
        mockWeather.setWeatherDataList(Collections.emptyList());
        mockWeather.setTimeStamp(ZonedDateTime.now(ZoneId.of("UTC")));
        mockWeather.setId(UUID.randomUUID());

        LatestWeatherApiEntity mockEntity = new LatestWeatherApiEntity();
        mockEntity.setLatestWeather(mockWeather);
        mockEntity.setCity(mockCity);
        mockEntity.setSmhi(smhi);
        mockEntity.setYr(yr);
        mockEntity.setFmi(fmi);

        return mockEntity;
    }

    private CityEntity createMockCity(String cityName) {
        CityEntity mockCity = new CityEntity();
        mockCity.setName(cityName);
        mockCity.setId(UUID.randomUUID());
        mockCity.setLon(0.0);
        mockCity.setLat(0.0);
        return mockCity;
    }
}
