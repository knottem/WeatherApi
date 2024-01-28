package com.example.weatherapi.util;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.entities.CityEntity;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.example.weatherapi.util.CityMapper.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class CityMapperTests {


    // Test Case 2: Check that the toEntity method returns the correct CityEntity object
    // Old name: toEntityTest_Valid
    @Test
    void shouldConvertCityToCityEntity() {
        // Arrange
        City city = City.builder()
                .name("Stockholm")
                .lat(59.3294)
                .lon(18.0686)
                .build();
        CityEntity cityEntity = toEntity(city);
        // Assert
        assertThat(city.getName()).isEqualTo(cityEntity.getName());
        assertThat(city.getLat()).isEqualTo(cityEntity.getLat());
        assertThat(city.getLon()).isEqualTo(cityEntity.getLon());
    }

    // Test Case 2: Check that the toModel method returns the correct City object
    // Old name: toModelTest_Valid
    @Test
    void shouldConvertCityEntityToCity() {
        // Arrange
        CityEntity cityEntity = CityEntity.builder()
                .name("Stockholm")
                .lat(59.3294)
                .lon(18.0686)
                .build();
        City city = toModel(cityEntity);
        // Assert
        assertThat(city.getName()).isEqualTo(cityEntity.getName());
        assertThat(city.getLat()).isEqualTo(cityEntity.getLat());
        assertThat(city.getLon()).isEqualTo(cityEntity.getLon());
    }
}
