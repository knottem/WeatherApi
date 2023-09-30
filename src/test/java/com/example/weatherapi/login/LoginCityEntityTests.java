package com.example.weatherapi.login;

import com.example.weatherapi.domain.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;

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
public class LoginCityEntityTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    /**
     * Test Case 1: forbidden request to city by a user that is not admin<br>
     * Asserts that the response is correct and that the user is not admin
     */
    @Test
    public void shouldReturnForbiddenStatusForUser(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("user", "pass123")
                .getForEntity("http://localhost:" + port + "/city/Stockholm", ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Forbidden");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getBody().getPath()).isEqualTo("/city/Stockholm");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    /**
     * Test Case 2: unauthorized request to city by a user that is not logged in<br>
     * Asserts that the response is correct and that the user is not logged in
     */
    @Test
    public void shouldReturnUnauthorizedStatusForNoAuth(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .getForEntity("http://localhost:" + port + "/city/Stockholm", ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getBody().getPath()).isEqualTo("/city/Stockholm");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    /**
     * Test Case 3: unauthorized request to city by a user with wrong password<br>
     * Asserts that the response is correct and that the user is not logged in
     * shouldReturnUnauthorizedStatusForWrongPassword
     */
    @Test
    public void shouldReturnUnauthorizedStatusForWrongPassword(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "wrongpassword")
                .getForEntity("http://localhost:" + port + "/city/Stockholm", ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getBody().getPath()).isEqualTo("/city/Stockholm");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }
}
