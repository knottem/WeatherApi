package com.example.weatherapi.api;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.entities.CityEntity;
import com.example.weatherapi.util.CityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.example.weatherapi.utilitytests.WeatherTestUtils.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SmhiApiTests {

    @Autowired
    private SmhiApi smhiApi;

    @BeforeEach
    public void setupBeforeEach() {
        smhiApi.setTestMode(true);
    }

    // Test Case 1: Check that the getWeatherSmhi method returns the correct Weather object
    @Test
    public void getWeatherSmhiTestStockholm_Valid() {
        // Arrange
        CityEntity city = new CityEntity(1L, "Stockholm", 59.3294, 18.0686);
        // Assert
        assertWeatherDataSmhiStockholm(smhiApi.getWeatherSmhi(city.getLon(), city.getLat(), city));
    }

    // Test Case 2: Check that the getWeatherSmhi method returns the correct Weather object for Gothenburg
    @Test
    public void getWeatherSmhiTestGothenburg_Valid() {
        // Arrange
        City city = new City("Göteborg", 57.7089, 11.9746);
        // Assert
        assertWeatherDataSmhiGothenburg(smhiApi.getWeatherSmhi(city.getLon(), city.getLat(), CityMapper.toEntity(city)));
    }

}
