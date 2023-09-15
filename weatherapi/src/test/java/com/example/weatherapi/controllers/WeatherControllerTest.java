package com.example.weatherapi.controllers;

import com.example.weatherapi.api.SmhiApi;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WeatherControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SmhiApi smhiApi;

    @BeforeEach
    public void setupBeforeEach() {
        smhiApi.setTestMode(true);
    }

    // Test Case 1: Check that the response is correct
    @Test
    public void getWeatherByCityTest_Valid() {
        ResponseEntity<Weather> response = restTemplate
                .withBasicAuth("user", "pass123")
                .getForEntity("http://localhost:" + port + "/weather/smhi/Stockholm", Weather.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).getMessage())
                .isEqualTo("Weather for Stockholm with location Lon: 18.0686 and Lat: 59.3294");
        assertThat(response.getBody().getInformation().getTemperature()).isEqualTo("Celsius");
        assertThat(response.getBody().getInformation().getWindSpeed()).isEqualTo("m/s");
        assertThat(response.getBody().getInformation().getPrecipitation()).isEqualTo("mm/hr - kg/m2/h");
        assertThat(response.getBody().getInformation().getTime()).isEqualTo("UTC");

        Weather.WeatherData firstWeatherData = response.getBody().getWeatherData()
                .get(response.getBody().getWeatherData().keySet().iterator().next());

        assertThat(firstWeatherData.getTemperature()).isEqualTo(15.7f);
        assertThat(firstWeatherData.getWeatherCode()).isEqualTo(6);
        assertThat(firstWeatherData.getWindSpeed()).isEqualTo(4.6f);
        assertThat(firstWeatherData.getWindDirection()).isEqualTo(198.0f);
        assertThat(firstWeatherData.getPrecipitation()).isEqualTo(0.0f);
    }

    // Test Case 2: Check case sensitivity
    @Test
    public void getWeatherByCityTest_CaseSensitivity() {
        ResponseEntity<Weather> response = restTemplate
                .withBasicAuth("user", "pass123")
                .getForEntity("http://localhost:" + port + "/weather/smhi/StoCkHolm", Weather.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).getMessage())
                .isEqualTo("Weather for Stockholm with location Lon: 18.0686 and Lat: 59.3294");
        assertThat(response.getBody().getInformation().getTemperature()).isEqualTo("Celsius");
        assertThat(response.getBody().getInformation().getWindSpeed()).isEqualTo("m/s");
        assertThat(response.getBody().getInformation().getPrecipitation()).isEqualTo("mm/hr - kg/m2/h");
        assertThat(response.getBody().getInformation().getTime()).isEqualTo("UTC");

        Weather.WeatherData firstWeatherData = response.getBody().getWeatherData()
                .get(response.getBody().getWeatherData().keySet().iterator().next());

        assertThat(firstWeatherData.getTemperature()).isEqualTo(15.7f);
        assertThat(firstWeatherData.getWeatherCode()).isEqualTo(6);
        assertThat(firstWeatherData.getWindSpeed()).isEqualTo(4.6f);
        assertThat(firstWeatherData.getWindDirection()).isEqualTo(198.0f);
        assertThat(firstWeatherData.getPrecipitation()).isEqualTo(0.0f);
    }


}
