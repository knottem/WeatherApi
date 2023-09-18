package com.example.weatherapi.util;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.entities.CityEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.example.weatherapi.util.CityMapper.toEntity;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CityMapperTests {

    //Test Case 1: Check that the toEntity method returns the correct CityEntity object
    @Test
    public void toEntityTest_Valid() {
        // Arrange
        City city = new City("Stockholm", 59.3294, 18.0686);
        CityEntity cityEntity = toEntity(city);
        // Assert
        assertThat(city.getName()).isEqualTo(cityEntity.getName());
        assertThat(city.getLat()).isEqualTo(cityEntity.getLat());
        assertThat(city.getLon()).isEqualTo(cityEntity.getLon());
    }

    // Test Case 1: Check that the toModel method returns the correct City object
    @Test
    public void toModelTest_Valid() {
        // Arrange
        CityEntity cityEntity = new CityEntity(1L, "Stockholm", 59.3294, 18.0686);
        City city = CityMapper.toModel(cityEntity);
        // Assert
        assertThat(city.getName()).isEqualTo(cityEntity.getName());
        assertThat(city.getLat()).isEqualTo(cityEntity.getLat());
        assertThat(city.getLon()).isEqualTo(cityEntity.getLon());
    }
}
