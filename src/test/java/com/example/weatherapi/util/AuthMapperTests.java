package com.example.weatherapi.util;

import com.example.weatherapi.domain.Auth;
import com.example.weatherapi.domain.UserRole;
import com.example.weatherapi.domain.entities.AuthEntity;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


/**
 * This class contains tests for the AuthMapper class.
 * It includes tests for creating an AuthMapper, and converting to and from AuthEntity objects.
 * <p>
 * Each test is annotated with {@code @Test}, which lets JUnit know to run the method as a test case.
 *
 *  @author Erik Wallenius
 *  @see <a href="https://github.com/knottem/WeatherApi">Repository Link</a>
 */
public class AuthMapperTests {

    /**
     * Test Case 1: Check that the AuthMapper is created.<p>
     * Asserts that a new AuthMapper object is not null.
     */
    @Test
    public void shouldCreateAuthMapper() {
        assertThat(new AuthMapper()).isNotNull();
    }

    /**
     * Test Case 2: Check that the toEntity method returns the correct AuthEntity object.
     * <p>
     * Arrange: An Auth object is created with username, password, and role.<br>
     * Act: The Auth object is converted to an AuthEntity object.<br>
     * Assert: The username, password, and role are verified to be equal between the Auth and AuthEntity objects.
     */
    @Test
    public void shouldConvertAuthToAuthEntity() {
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

    /**
     * Test Case 3: Check that the toModel method returns the correct Auth object.
     * <p>
     * Arrange: An AuthEntity object is created with username, password, and role.<br>
     * Act: The AuthEntity object is converted to an Auth object.<br>
     * Assert: The username, password, and role are verified to be equal between the Auth and AuthEntity objects.
     */
    @Test
    public void shouldConvertAuthEntityToAuth() {
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
