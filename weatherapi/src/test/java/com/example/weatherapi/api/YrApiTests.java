package com.example.weatherapi.api;

import com.example.weatherapi.domain.entities.CityEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.example.weatherapi.util.WeatherTestUtils.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class YrApiTests {

    @Autowired
    private YrApi yrApi;

    @BeforeEach
    public void setupBeforeEach() {
        yrApi.setTestMode(true);
    }

    // Test Case 1: Check that the getWeatherSmhi method returns the correct Weather object for Stockholm
    @Test
    public void getWeatherYrTestStockholm_Valid() {
        // Arrange
        CityEntity city = new CityEntity(1L, "Stockholm", 59.3294, 18.0686);
        // Assert
        assertWeatherDataYrStockholm(yrApi.getWeatherYr(city.getLon(), city.getLat(), city));
    }

    // Test Case 2: Check that the getWeatherYr method returns the correct Weather object for Gothenburg
    @Test
    public void getWeatherYrTestGothenburg_Valid() {
        // Arrange
        CityEntity city = new CityEntity(1L, "Göteborg", 57.7089, 11.9746);
        // Assert
        assertWeatherDataYrGothenburg(yrApi.getWeatherYr(city.getLon(), city.getLat(), city));
    }



}

