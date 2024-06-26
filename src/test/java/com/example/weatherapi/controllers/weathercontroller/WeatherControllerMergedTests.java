package com.example.weatherapi.controllers.weathercontroller;

import com.example.weatherapi.api.FmiApi;
import com.example.weatherapi.api.SmhiApi;
import com.example.weatherapi.api.YrApi;
import com.example.weatherapi.domain.ErrorResponse;
import com.example.weatherapi.domain.weather.Weather;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

import static com.example.weatherapi.utilitytests.WeatherTestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WeatherControllerMergedTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SmhiApi smhiApi;

    @Autowired
    private YrApi yrApi;

    @Autowired
    private FmiApi fmiApi;

    @BeforeEach
    public void setupBeforeEach() {
        smhiApi.setTestMode(true);
        yrApi.setTestMode(true);
        fmiApi.setTestMode(true);
    }

    // Test Case 1: Check that the response is correct
    @Test
    void getWeatherByCityMergedTest_Valid() {
        ResponseEntity<Weather> response = restTemplate
                .getForEntity("http://localhost:" + port + "/api/v1/weather/Stockholm", Weather.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).getMessage())
                .isEqualTo("Merged weather for Stockholm from FMI, SMHI and YR");
        assertWeatherInformation(response.getBody());
        assertWeatherDataMergedStockholm(response.getBody());
    }

    // Test Case 2: Check case sensitivity
    @Test
    void getWeatherByCityMergedTest_CaseSensitivity() {
        ResponseEntity<Weather> response = restTemplate
                .getForEntity("http://localhost:" + port + "/api/v1/weather/StoCKHOLM", Weather.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).getMessage())
                .isEqualTo("Merged weather for Stockholm from FMI, SMHI and YR");
        assertWeatherInformation(response.getBody());
        assertWeatherDataMergedStockholm(response.getBody());
    }
    // Test Case 3: Check that the response is correct with a city with unicode characters
    @Test
    void getWeatherByCityMergedTest_WithUnicode() {
        ResponseEntity<Weather> response = restTemplate
                .getForEntity("http://localhost:" + port + "/api/v1/weather/göteborg", Weather.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).getMessage())
                .isEqualTo("Merged weather for Göteborg from SMHI and YR");
        assertWeatherInformation(response.getBody());
        assertWeatherDataMergedGothenburg(response.getBody());
    }

    // Test Case 4: Check that the response is correct if the city is not found
    @Test
    void getWeatherByCityMergedTest_NotFound() {
        String cityToTest = "Stockholm123";
        ResponseEntity<ErrorResponse> response = restTemplate
                .getForEntity("http://localhost:" + port + "/api/v1/weather/" + cityToTest, ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("City not found: " + cityToTest.toLowerCase());
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/weather/" + cityToTest);
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(ZonedDateTime.now(ZoneId.of("UTC")));
    }

    // Test Case 5: Hit the endpoint several times to make sure the cache works
    @Test
    void getWeatherByCityMergedTest_SeveralAttempts(){
        for (int i = 0; i < 10; i++) {
            ResponseEntity<Weather> response = restTemplate
                    .getForEntity("http://localhost:" + port + "/api/v1/weather/Stockholm", Weather.class);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(Objects.requireNonNull(response.getBody()).getMessage())
                    .isEqualTo("Merged weather for Stockholm from FMI, SMHI and YR");
            assertWeatherInformation(response.getBody());
            assertWeatherDataMergedStockholm(response.getBody());
        }
    }

}
