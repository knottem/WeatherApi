package com.example.weatherapi.api;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.entities.CityEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.example.weatherapi.utilitytests.WeatherTestUtils.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class YrApiTests {

    @Autowired
    private YrApi yrApi;

    @BeforeEach
    public void setupBeforeEach() {
        yrApi.setTestMode(true);
    }

    // Test Case 1: Check that the getWeatherSmhi method returns the correct Weather object for Stockholm
    @Test
    void getWeatherYrTestStockholm_Valid() {
        // Arrange
        City city = new City("Stockholm", 59.3294, 18.0686, null, null);
        // Assert
        assertWeatherDataYrStockholm(yrApi.getWeatherYr(city.getLon(), city.getLat(), city));
    }

    // Test Case 2: Check that the getWeatherYr method returns the correct Weather object for Gothenburg
    @Test
    void getWeatherYrTestGothenburg_Valid() {
        // Arrange
        City city = new City("Göteborg", 57.7089, 11.9746, null, null);
        // Assert
        assertWeatherDataYrGothenburg(yrApi.getWeatherYr(city.getLon(), city.getLat(), city));
    }

    // Test Case 3: Check that the getWeatherYr method returns the correct Weather when raining
    @Test
    void getWeatherYrTestRain_Valid() {
        // Arrange
        City city = new City("Rågsved", 59.3294, 18.0686, null, null);
        // Assert
        assertWeatherDataYrRain(yrApi.getWeatherYr(city.getLon(), city.getLat(), city));
    }

    // Test Case 4: Check that the getWeatherYr method returns the correct Weather when raining, later time
    @Test
    void getWeatherYrTestRain_Valid2() {
        // Arrange
        City city = new City("Rågsved", 59.3294, 18.0686, null, null);
        // Assert
        assertWeatherDataYrRainLater(yrApi.getWeatherYr(city.getLon(), city.getLat(), city));
    }



}

