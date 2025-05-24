package com.example.weatherapi.login;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class contains tests for the login functionality of the CityController.
 * <p>
 * Each test is annotated with {@code @Test}, which lets JUnit know to run the method as a test case.
 *
 *  @author Erik Wallenius
 *  @see <a href="https://github.com/knottem/WeatherApi">Repository Link</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LoginCityEntityTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private final String baseUrl = "http://localhost:";
    private final String endpoint = "/api/v1/city";
    /**
     * Test Case 1: forbidden request to city by a user that is not admin<br>
     * Asserts that the response is correct and that the user is not admin
     */
    @Test
    void shouldReturnForbiddenStatusForUser(){
        ResponseEntity<ProblemDetail> response = restTemplate
                .withBasicAuth("user", "pass123")
                .getForEntity(baseUrl + port + endpoint + "/stockholm", ProblemDetail.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Forbidden");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(Objects.requireNonNull(response.getBody().getInstance()).toString()).isEqualTo(endpoint + "/stockholm");
    }

    /**
     * Test Case 2: unauthorized request to city by a user that is not logged in<br>
     * Asserts that the response is correct and that the user is not logged in
     */
    @Test
    void shouldReturnUnauthorizedStatusForNoAuth(){
        ResponseEntity<ProblemDetail> response = restTemplate
                .getForEntity(baseUrl + port + endpoint + "/Stockholm", ProblemDetail.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Unauthorized");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    /**
     * Test Case 3: unauthorized request to city by a user with wrong password<br>
     * Asserts that the response is correct and that the user is not logged in
     * shouldReturnUnauthorizedStatusForWrongPassword
     */
    @Test
    void shouldReturnUnauthorizedStatusForWrongPassword(){
        ResponseEntity<ProblemDetail> response = restTemplate
                .withBasicAuth("admin", "wrongpassword")
                .getForEntity(baseUrl + port + endpoint + "/Stockholm", ProblemDetail.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Unauthorized");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }
}
