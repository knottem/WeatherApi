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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LoginCityEntityTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    // Test Case 1: forbidden request to city by a user that is not admin
    @Test
    public void forbiddenRequestToCity(){
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

    // Test Case 2: no auth request to city
    @Test
    public void noAuthRequestToCity(){
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

    // Test Case 3: Wrong password
    @Test
    public void wrongPassword(){
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
