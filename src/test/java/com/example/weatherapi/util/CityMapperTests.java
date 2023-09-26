package com.example.weatherapi.util;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.entities.CityEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CityMapperTests {


    // Test Case 1: Check that the CityMapper is created
    @Test
    public void shouldCreateCityMapper() {
        assertThat(new CityMapper()).isNotNull();
    }

    // Test Case 2: Check that the toEntity method returns the correct CityEntity object
    // Old name: toEntityTest_Valid
    @Test
    public void shouldConvertCityToCityEntity() {
        // Arrange
        City city = City.builder()
                .name("Stockholm")
                .lat(59.3294)
                .lon(18.0686)
                .build();
        CityEntity cityEntity = CityMapper.toEntity(city);
        // Assert
        assertThat(city.getName()).isEqualTo(cityEntity.getName());
        assertThat(city.getLat()).isEqualTo(cityEntity.getLat());
        assertThat(city.getLon()).isEqualTo(cityEntity.getLon());
    }

    // Test Case 2: Check that the toModel method returns the correct City object
    // Old name: toModelTest_Valid
    @Test
    public void shouldConvertCityEntityToCity() {
        // Arrange
        CityEntity cityEntity = CityEntity.builder()
                .name("Stockholm")
                .lat(59.3294)
                .lon(18.0686)
                .build();
        City city = CityMapper.toModel(cityEntity);
        // Assert
        assertThat(city.getName()).isEqualTo(cityEntity.getName());
        assertThat(city.getLat()).isEqualTo(cityEntity.getLat());
        assertThat(city.getLon()).isEqualTo(cityEntity.getLon());
    }
}
