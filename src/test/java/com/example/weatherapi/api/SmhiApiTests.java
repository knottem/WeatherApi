package com.example.weatherapi.api;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.entities.CityEntity;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.util.CityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.example.weatherapi.utilitytests.WeatherTestUtils.*;

/**
 * This class contains tests for the SmhiApi class.
 * <p>
 * @author Erik Wallenius
 * @see <a href="https://github.com/knottem/WeatherApi">Repository Link</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SmhiApiTests {

    @Autowired
    private SmhiApi smhiApi;

    /**
     * This method is run before each test case to set the test mode to true.<br>
     * This is done to avoid making calls to the SMHI API during testing.
     */
    @BeforeEach
    public void setupBeforeEach() {
        smhiApi.setTestMode(true);
    }

    /**
     * Test Case 1: Check that the getWeatherSmhi method returns the correct Weather object for Stockholm
     * <p>
     * Arrange: A CityEntity object is created with the name Stockholm and the coordinates for Stockholm.<br>
     * Act: The getWeatherSmhi method is called with the coordinates for Stockholm.<br>
     * Assert: The Weather object returned from the getWeatherSmhi method is verified to be equal to the Weather object for Stockholm.
     * <p>
     * {@link com.example.weatherapi.utilitytests.WeatherTestUtils#assertWeatherDataSmhiStockholm(Weather)} is used for the assertion.
     */
    @Test
    void getWeatherSmhiTestStockholm_Valid() {
        // Arrange
        City city = new City("Stockholm", 59.3294, 18.0686);
        // Assert
        assertWeatherDataSmhiStockholm(smhiApi.getWeatherSmhi(city.getLon(), city.getLat(), city));
    }

    /**
     * Test Case 2: Check that the getWeatherSmhi method returns the correct Weather object for Gothenburg
     * <p>
     * Arrange: A CityEntity object is created with the name Göteborg and the coordinates for Göteborg.<br>
     * Act: The getWeatherSmhi method is called with the coordinates for Göteborg.<br>
     * Assert: The Weather object returned from the getWeatherSmhi method is verified to be equal to the Weather object for Göteborg.
     * <p>
     * {@link com.example.weatherapi.utilitytests.WeatherTestUtils#assertWeatherDataSmhiGothenburg(Weather)} is used for the assertion.
     */
    @Test
    void getWeatherSmhiTestGothenburg_Valid() {
        // Arrange
        City city = new City("Göteborg", 57.7089, 11.9746);
        // Assert
        assertWeatherDataSmhiGothenburg(smhiApi.getWeatherSmhi(city.getLon(), city.getLat(), city));
    }

}
