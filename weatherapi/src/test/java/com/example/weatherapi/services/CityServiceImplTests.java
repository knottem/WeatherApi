package com.example.weatherapi.services;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.entities.CityEntity;
import com.example.weatherapi.exceptions.exceptions.CityNotFoundException;
import com.example.weatherapi.exceptions.exceptions.InvalidCityException;
import com.example.weatherapi.services.impl.CityServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CityServiceImplTests {

    @Autowired
    private CityServiceImpl cityService;

    // Test Case 1: Get city by name
    @Test
    public void getCityByNameTest_OK() {
        CityEntity city = cityService.getCityByName("stockholm");
        assertThat(city.getId()).isEqualTo(1L);
        assertThat(city.getName()).isEqualTo("Stockholm");
        assertThat(city.getLon()).isEqualTo(18.0686);
        assertThat(city.getLat()).isEqualTo(59.3294);
    }

    // Test Case 2: Try to get city by name that does not exist
    @Test
    public void getCityByNameTest_NotFound() {
        // Assert that the exception is thrown
        CityNotFoundException exception = assertThrows(CityNotFoundException.class, () -> {
            cityService.getCityByName("notfound");
        });
        // Assert the exception message
        assertThat(exception.getMessage()).isEqualTo("City not found: notfound");
    }

    //Test Case 3: Try to get city by name that is null
    @Test
    public void getCityByNameTest_Null() {
        // Assert that the exception is thrown
        CityNotFoundException exception = assertThrows(CityNotFoundException.class, () -> {
            cityService.getCityByName(null);
        });
        // Assert the exception message
        assertThat(exception.getMessage()).isEqualTo("City not found: null");
    }

    //Test Case 4: add city with valid values
    @Test
    public void addCityTest_OK() {
        CityEntity addedCity = cityService.addCity(City.builder()
                .name("TestCity2")
                .lon(18.0686)
                .lat(59.3294)
                .build());

        assertThat(addedCity.getName()).isEqualTo("TestCity2");
        assertThat(addedCity.getLon()).isEqualTo(18.0686);
        assertThat(addedCity.getLat()).isEqualTo(59.3294);
    }

    //Test Case 5: add city with null values
    @Test
    public void addCityTest_Null() {
        // Assert that the exception is thrown
        InvalidCityException exception = assertThrows(InvalidCityException.class, () -> {
            cityService.addCity(null);
        });
        // Assert the exception message
        assertThat(exception.getMessage()).isEqualTo("City cannot be null");
    }

    //Test Case 6: add city with null name
    @Test
    public void addCityTest_NullName() {
        // Assert that the exception is thrown
        InvalidCityException exception = assertThrows(InvalidCityException.class, () -> {
            cityService.addCity(City.builder()
                    .name(null)
                    .lon(18.0686)
                    .lat(59.3294)
                    .build());
        });
        // Assert the exception message
        assertThat(exception.getMessage()).isEqualTo("City name cannot be null or empty");
    }

    //Test Case 7: add city with empty name
    @Test
    public void addCityTest_EmptyName() {
        // Assert that the exception is thrown
        InvalidCityException exception = assertThrows(InvalidCityException.class, () -> {
            cityService.addCity(City.builder()
                    .name("")
                    .lon(18.0686)
                    .lat(59.3294)
                    .build());
        });
        // Assert the exception message
        assertThat(exception.getMessage()).isEqualTo("City name cannot be null or empty");
    }

    //Test Case 8: add city with null longitude
    @Test
    public void addCityTest_NullLon() {
        // Assert that the exception is thrown
        InvalidCityException exception = assertThrows(InvalidCityException.class, () -> {
            cityService.addCity(City.builder()
                    .name("TestCity2")
                    .lon(null)
                    .lat(59.3294)
                    .build());
        });
        // Assert the exception message
        assertThat(exception.getMessage()).isEqualTo("Longitude cannot be null");
    }

    //Test Case 9: add city with null latitude
    @Test
    public void addCityTest_NullLat() {
        // Assert that the exception is thrown
        InvalidCityException exception = assertThrows(InvalidCityException.class, () -> {
            cityService.addCity(City.builder()
                    .name("TestCity2")
                    .lon(18.0686)
                    .lat(null)
                    .build());
        });
        // Assert the exception message
        assertThat(exception.getMessage()).isEqualTo("Latitude cannot be null");
    }

    //Test Case 10: add city with invalid longitude
    @Test
    public void addCityTest_InvalidLon() {
        // Assert that the exception is thrown
        InvalidCityException exception = assertThrows(InvalidCityException.class, () -> {
            cityService.addCity(City.builder()
                    .name("TestCity2")
                    .lon(181.0686)
                    .lat(59.3294)
                    .build());
        });
        // Assert the exception message
        assertThat(exception.getMessage()).isEqualTo("Invalid longitude value: 181.0686, must be between -180 and 180");
    }

    //Test Case 11: add city with invalid latitude
    @Test
    public void addCityTest_InvalidLat() {
        // Assert that the exception is thrown
        InvalidCityException exception = assertThrows(InvalidCityException.class, () -> {
            cityService.addCity(City.builder()
                    .name("TestCity2")
                    .lon(18.0686)
                    .lat(91.3294)
                    .build());
        });
        // Assert the exception message
        assertThat(exception.getMessage()).isEqualTo("Invalid latitude value: 91.3294, must be between -90 and 90");
    }

    //Test Case 12: add city with existing name
    @Test
    public void addCityTest_ExistingName() {
        // Assert that the exception is thrown
        InvalidCityException exception = assertThrows(InvalidCityException.class, () -> {
            cityService.addCity(City.builder()
                    .name("Stockholm")
                    .lon(18.0686)
                    .lat(59.3294)
                    .build());
        });
        // Assert the exception message
        assertThat(exception.getMessage()).isEqualTo("City already exists: Stockholm");
    }

}
