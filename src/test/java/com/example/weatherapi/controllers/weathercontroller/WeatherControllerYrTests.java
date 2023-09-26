package com.example.weatherapi.controllers.weathercontroller;

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
import java.util.Objects;

import static com.example.weatherapi.utilitytests.WeatherTestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WeatherControllerYrTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private YrApi yrApi;

    @BeforeEach
    public void setupBeforeEach() {
        yrApi.setTestMode(true);
    }

    // Test Case 1: Check that the response is correct
    @Test
    public void getWeatherByCityYrTest_Valid() {
        ResponseEntity<Weather> response = restTemplate
                .withBasicAuth("user", "pass123")
                .getForEntity("http://localhost:" + port + "/weather/yr/Stockholm", Weather.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).getMessage())
                .isEqualTo("Weather for Stockholm with location Lon: 18.0686 and Lat: 59.3294");
        assertWeatherInformation(response.getBody());
        assertWeatherDataYrStockholm(response.getBody());
    }

    // Test Case 2: Check case sensitivity
    @Test
    public void getWeatherByCityYrTest_CaseSensitivity() {
        ResponseEntity<Weather> response = restTemplate
                .withBasicAuth("user", "pass123")
                .getForEntity("http://localhost:" + port + "/weather/yr/StoCkHOLM", Weather.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).getMessage())
                .isEqualTo("Weather for Stockholm with location Lon: 18.0686 and Lat: 59.3294");
        assertWeatherInformation(response.getBody());
        assertWeatherDataYrStockholm(response.getBody());
    }

    // Test Case 3: Check that the response is correct with a city with unicode characters
    @Test
    public void getWeatherByCityYrTest_WithUnicode() {
        ResponseEntity<Weather> response = restTemplate
                .withBasicAuth("user", "pass123")
                .getForEntity("http://localhost:" + port + "/weather/yr/göteborg", Weather.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).getMessage())
                .isEqualTo("Weather for Göteborg with location Lon: 11.9675 and Lat: 57.7075");
        assertWeatherInformation(response.getBody());
        assertWeatherDataYrGothenburg(response.getBody());
    }

    // Test Case 4: Check that the response is correct if the city is not found
    @Test
    public void getWeatherByCityYrTest_NotFound() {
        String cityToTest = "Stockholm123";
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("user", "pass123")
                .getForEntity("http://localhost:" + port + "/weather/yr/" + cityToTest, ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("City not found: " + cityToTest);
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getBody().getPath()).isEqualTo("/weather/yr/" + cityToTest);
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

}
