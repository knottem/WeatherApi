package com.example.weatherapi.util;

import com.example.weatherapi.domain.Auth;
import com.example.weatherapi.domain.UserRole;
import com.example.weatherapi.domain.entities.AuthEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthMapperTests {

    // Test Case 1: Check that the toEntity method returns the correct AuthEntity object
    @Test
    public void toEntityTest_Valid() {
        // Arrange
        Auth auth = Auth.builder()
                .username("test")
                .password("test")
                .role(UserRole.USER)
                .build();

        // Act
        AuthEntity authEntity = AuthMapper.toEntity(auth);

        // Assert
        assertThat(auth.getUsername()).isEqualTo(authEntity.getUsername());
        assertThat(auth.getPassword()).isEqualTo(authEntity.getPassword());
        assertThat(auth.getRole()).isEqualTo(authEntity.getRole());
    }

    // Test Case 2: Check that the toModel method returns the correct Auth object
    @Test
    public void toModelTest_Valid() {
        // Arrange
        AuthEntity authEntity = AuthEntity.builder()
                .username("test")
                .password("test")
                .role(UserRole.USER)
                .build();

        // Act
        Auth auth = AuthMapper.toModel(authEntity);

        // Assert
        assertThat(auth.getUsername()).isEqualTo(authEntity.getUsername());
        assertThat(auth.getPassword()).isEqualTo(authEntity.getPassword());
        assertThat(auth.getRole()).isEqualTo(authEntity.getRole());
    }
}
