package com.example.weatherapi.controllers;

import com.example.weatherapi.domain.ErrorResponse;
import com.example.weatherapi.domain.entities.Auth;
import org.junit.jupiter.api.Disabled;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Disabled // Disabled because we are writing tests before implementing the functionality
public class AuthControllerTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    // Test Case 1: Forbidden request to auth by a user that is not admin
    @Test
    public void getAuthListByUserTest_Forbidden() {
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("user", "pass123")
                .getForEntity("http://localhost:" + port + "/auth/list", ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Forbidden");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getBody().getPath()).isEqualTo("/auth/list");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 2: OK request to auth by a user that is admin
    @Test
    public void getAuthListByAdminTest_OK() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .getForEntity("http://localhost:" + port + "/auth/list", String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo("List of users");
    }

    // Test Case 3: Retrieve specific user with role ADMIN
    @Test
    public void getAuthAdminByAdminTest_OK() {
        ResponseEntity<Auth> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .getForEntity("http://localhost:" + port + "/auth?user=admin", Auth.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("admin");
        assertThat(response.getBody().getRole()).isEqualTo(Auth.Role.ADMIN);
        assertThat(response.getBody().getPassword()).isNull();

    }

    // Test Case 4: Retrieve specific user with role USER
    @Test
    public void getAuthUserByAdminTest_OK() {
        ResponseEntity<Auth> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .getForEntity("http://localhost:" + port + "/auth?user=user", Auth.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("user");
        assertThat(response.getBody().getRole()).isEqualTo(Auth.Role.USER);
        assertThat(response.getBody().getPassword()).isNull();
    }

}
