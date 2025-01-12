package com.example.weatherapi.services;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.entities.CityEntity;
import com.example.weatherapi.exceptions.CityNotFoundException;
import com.example.weatherapi.exceptions.InvalidCityException;
import com.example.weatherapi.services.impl.CityServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CityServiceImplTests {

    @Autowired
    private CityServiceImpl cityService;

    // Test Case 1: Get city by name
    @Test
    void shouldReturnCityByName() {
        CityEntity city = cityService.getCityByName("stockholm");
        assertThat(city.getId()).isEqualTo(UUID.fromString("2785714e-0872-4a61-bfb5-76b5baf8911b"));
        assertThat(city.getName()).isEqualTo("Stockholm");
        assertThat(city.getLon()).isEqualTo(18.0686);
        assertThat(city.getLat()).isEqualTo(59.3294);
    }

    // Test Case 2: Try to get city by name that does not exist
    @Test
    void shouldThrowExceptionWhenCityNameNotFound() {
        // Assert that the exception is thrown
        CityNotFoundException exception = assertThrows(CityNotFoundException.class, () -> cityService.getCityByName("notfound"));
        // Assert the exception message
        assertThat(exception.getMessage()).isEqualTo("City not found: notfound");
    }

    //Test Case 3: Try to get city by name that is null
    @Test
    void shouldThrowExceptionWhenCityNameIsNull() {
        // Assert that the exception is thrown
        CityNotFoundException exception = assertThrows(CityNotFoundException.class, () -> cityService.getCityByName(null));
        // Assert the exception message
        assertThat(exception.getMessage()).isEqualTo("City not found: null");
    }

    //Test Case 4: add city with valid values
    @Test
    void shouldAddCityWithValidValues() {
        CityEntity addedCity = cityService.addCity(City.builder()
                .name("TestCity2")
                .lon(18.0686)
                .lat(59.3294)
                .build());

        assertThat(addedCity.getName()).isEqualTo("TestCity2");
        assertThat(addedCity.getLon()).isEqualTo(18.0686);
        assertThat(addedCity.getLat()).isEqualTo(59.3294);
    }

    //Test Case 5: add city with existing name
    @Test
    void shouldThrowExceptionWhenAddingCityWithExistingName() {
        // Assert that the exception is thrown
        InvalidCityException exception = assertThrows(InvalidCityException.class, () -> cityService.addCity(City.builder()
                .name("Stockholm")
                .lon(18.0686)
                .lat(59.3294)
                .build()));
        // Assert the exception message
        assertThat(exception.getMessage()).isEqualTo("City already exists: Stockholm");
    }

    //Test Case 6: get all cities
    @Test
    void shouldReturnAllCities() {
        assertThat(cityService.getAllCities().get(0).getName()).isEqualTo("Stockholm");
        assertThat(cityService.getAllCities().get(0).getLon()).isEqualTo(18.0686);
        assertThat(cityService.getAllCities().get(0).getLat()).isEqualTo(59.3294);
        assertThat(cityService.getAllCities().get(1).getName()).isEqualTo("Göteborg");
        assertThat(cityService.getAllCities().get(1).getLon()).isEqualTo(11.9675);
        assertThat(cityService.getAllCities().get(1).getLat()).isEqualTo(57.7075);

        // check that the number of cities is at least 10
        assertThat(cityService.getAllCities().size()).isGreaterThan(10);
    }

}
