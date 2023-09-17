package com.example.weatherapi.controllers.weathercontroller;

import com.example.weatherapi.api.YrApi;
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

import java.time.LocalDateTime;
import java.util.Objects;

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
        assertThat(response.getBody().getInformation().getTemperature()).isEqualTo("Celsius");
        assertThat(response.getBody().getInformation().getWindSpeed()).isEqualTo("m/s");
        assertThat(response.getBody().getInformation().getPrecipitation()).isEqualTo("mm/hr - kg/m2/h");
        assertThat(response.getBody().getInformation().getTime()).isEqualTo("UTC");

        assertThat(response.getBody().getWeatherData().get(LocalDateTime.parse("2023-09-17T10:00")))
                .isEqualTo(Weather.WeatherData.builder()
                        .temperature(15.0f)
                        .weatherCode(0)
                        .windSpeed(3.6f)
                        .windDirection(350.1f)
                        .precipitation(0.0f)
                        .build());
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
        assertThat(response.getBody().getInformation().getTemperature()).isEqualTo("Celsius");
        assertThat(response.getBody().getInformation().getWindSpeed()).isEqualTo("m/s");
        assertThat(response.getBody().getInformation().getPrecipitation()).isEqualTo("mm/hr - kg/m2/h");
        assertThat(response.getBody().getInformation().getTime()).isEqualTo("UTC");

        assertThat(response.getBody().getWeatherData().get(LocalDateTime.parse("2023-09-17T10:00")))
                .isEqualTo(Weather.WeatherData.builder()
                        .temperature(15.0f)
                        .weatherCode(0)
                        .windSpeed(3.6f)
                        .windDirection(350.1f)
                        .precipitation(0.0f)
                        .build());
    }

}
