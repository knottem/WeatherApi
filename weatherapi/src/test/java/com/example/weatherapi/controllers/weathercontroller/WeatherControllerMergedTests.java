package com.example.weatherapi.controllers.weathercontroller;

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

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WeatherControllerMergedTests {

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

    // Test Case 1: Check that the response is correct
    @Test
    public void getWeatherByCityMergedTest_Valid() {
        ResponseEntity<Weather> response = restTemplate
                .withBasicAuth("user", "pass123")
                .getForEntity("http://localhost:" + port + "/weather/merged/Stockholm", Weather.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).getMessage())
                .isEqualTo("Merged weather for Stockholm with location Lon: 18.0686 and Lat: 59.3294");
        assertThat(response.getBody().getInformation().getTemperature()).isEqualTo("Celsius");
        assertThat(response.getBody().getInformation().getWindSpeed()).isEqualTo("m/s");
        assertThat(response.getBody().getInformation().getPrecipitation()).isEqualTo("mm/hr - kg/m2/h");
        assertThat(response.getBody().getInformation().getTime()).isEqualTo("UTC");

        assertThat(response.getBody().getWeatherData().get(LocalDateTime.parse("2023-09-17T10:00")))
                .isEqualTo(Weather.WeatherData.builder()
                        .temperature(14.8f)
                        .weatherCode(2)
                        .windSpeed(3.5f)
                        .windDirection(348.55f)
                        .precipitation(0.0f)
                        .build());
    }

    // Test Case 2: Check case sensitivity
    @Test
    public void getWeatherByCityMergedTest_CaseSensitivity() {
        ResponseEntity<Weather> response = restTemplate
                .withBasicAuth("user", "pass123")
                .getForEntity("http://localhost:" + port + "/weather/merged/StoCKHOLM", Weather.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).getMessage())
                .isEqualTo("Merged weather for Stockholm with location Lon: 18.0686 and Lat: 59.3294");
        assertThat(response.getBody().getInformation().getTemperature()).isEqualTo("Celsius");
        assertThat(response.getBody().getInformation().getWindSpeed()).isEqualTo("m/s");
        assertThat(response.getBody().getInformation().getPrecipitation()).isEqualTo("mm/hr - kg/m2/h");
        assertThat(response.getBody().getInformation().getTime()).isEqualTo("UTC");

        assertThat(response.getBody().getWeatherData().get(LocalDateTime.parse("2023-09-17T10:00")))
                .isEqualTo(Weather.WeatherData.builder()
                        .temperature(14.8f)
                        .weatherCode(2)
                        .windSpeed(3.5f)
                        .windDirection(348.55f)
                        .precipitation(0.0f)
                        .build());
    }
    // Test Case 3: Check that the response is correct with a city with unicode characters
    @Test
    public void getWeatherByCityMergedTest_WithUnicode() {
        ResponseEntity<Weather> response = restTemplate
                .withBasicAuth("user", "pass123")
                .getForEntity("http://localhost:" + port + "/weather/merged/göteborg", Weather.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).getMessage())
                .isEqualTo("Merged weather for Göteborg with location Lon: 11.9675 and Lat: 57.7075");
        assertThat(response.getBody().getInformation().getTemperature()).isEqualTo("Celsius");
        assertThat(response.getBody().getInformation().getWindSpeed()).isEqualTo("m/s");
        assertThat(response.getBody().getInformation().getPrecipitation()).isEqualTo("mm/hr - kg/m2/h");
        assertThat(response.getBody().getInformation().getTime()).isEqualTo("UTC");

        assertThat(response.getBody().getWeatherData().get(LocalDateTime.parse("2023-09-17T15:00")))
                .isEqualTo(Weather.WeatherData.builder()
                        .temperature(19.75f)
                        .weatherCode(4)
                        .windSpeed(3.8f)
                        .windDirection(80.95f)
                        .precipitation(0.0f)
                        .build());
    }

    // Test Case 4: Check that the response is correct if the city is not found
    @Test
    public void getWeatherByCityMergedTest_NotFound() {
        String cityToTest = "Stockholm123";
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("user", "pass123")
                .getForEntity("http://localhost:" + port + "/weather/merged/" + cityToTest, ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("City not found: " + cityToTest);
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getBody().getPath()).isEqualTo("/weather/merged/" + cityToTest);
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

}
