package com.example.weatherapi.login;

import com.example.weatherapi.api.SmhiApi;
import com.example.weatherapi.api.YrApi;
import com.example.weatherapi.domain.ErrorResponse;
import com.example.weatherapi.domain.weather.Weather;
import org.junit.jupiter.api.BeforeEach;
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
public class LoginWeatherTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SmhiApi smhiApi;

    @Autowired
    private YrApi yrApi;

    @BeforeEach
    public void setupBeforeEach() {
        smhiApi.setTestMode(true);
        yrApi.setTestMode(true);
    }
    // Test Case 1: OK request to weather by a user with no admin rights
    @Test
    public void okRequestToWeatherUser(){
        ResponseEntity<Weather> response = restTemplate
                .withBasicAuth("user", "pass123")
                .getForEntity("http://localhost:" + port + "/weather/smhi/Stockholm", Weather.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // Test Case 2: OK request to weather by a user that is admin
    @Test
    public void okRequestToWeatherAdmin(){
        ResponseEntity<Weather> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .getForEntity("http://localhost:" + port + "/weather/smhi/Stockholm", Weather.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // Test Case 3: Unauthorized request to weather by a user that doesn't exist
    @Test
    public void unauthorizedRequestNoUserExist(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("noExist", "pass")
                .getForEntity("http://localhost:" + port + "/weather/smhi/Stockholm", ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getBody().getPath()).isEqualTo("/weather/smhi/Stockholm");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 4: Unauthorized request to weather by a user with wrong password
    @Test
    public void unauthorizedRequestWrongPasswordUser(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("user", "wrongpassword")
                .getForEntity("http://localhost:" + port + "/weather/smhi/Stockholm", ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getBody().getPath()).isEqualTo("/weather/smhi/Stockholm");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 4: Unauthorized request to weather by an admin with wrong password
    @Test
    public void unauthorizedRequestWrongPasswordAdmin(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "wrongpassword")
                .getForEntity("http://localhost:" + port + "/weather/smhi/Stockholm", ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getBody().getPath()).isEqualTo("/weather/smhi/Stockholm");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 5: No auth request to weather
    @Test
    public void noAuthRequestToWeather(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .getForEntity("http://localhost:" + port + "/weather/smhi/Stockholm", ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getBody().getPath()).isEqualTo("/weather/smhi/Stockholm");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 6: Empty username
    @Test
    public void emptyUsernameRequest(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("", "pass123")
                .getForEntity("http://localhost:" + port + "/weather/smhi/Stockholm", ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getBody().getPath()).isEqualTo("/weather/smhi/Stockholm");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }


    // Test Case 7: Empty password
    @Test
    public void emptyPasswordRequest(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("user", "")
                .getForEntity("http://localhost:" + port + "/weather/smhi/Stockholm", ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getBody().getPath()).isEqualTo("/weather/smhi/Stockholm");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }
}
