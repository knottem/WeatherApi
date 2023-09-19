package com.example.weatherapi.controllers;

import com.example.weatherapi.domain.UserRole;
import com.example.weatherapi.domain.entities.AuthEntity;
import com.example.weatherapi.domain.entities.CityEntity;
import com.example.weatherapi.domain.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CityEntityControllerTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    //Valid numbers
    private static final double validLat = 59.3294;
    private static final double validLon = 18.0686;

    // Test Case 1: retrieve city that exists
    @Test
    public void retrieveCityTestValid() {
        ResponseEntity<CityEntity> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .getForEntity("http://localhost:" + port + "/city/Stockholm", CityEntity.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getName()).isEqualTo("Stockholm");
        assertThat(response.getBody().getLat()).isEqualTo(59.3294);
        assertThat(response.getBody().getLon()).isEqualTo(18.0686);
    }

    // Test Case 2: retrieve city that doesn't exist
    @Test
    public void retrieveCityTestFaulty() {
        String cityToTest = "CITYNOTFOUND";

        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .getForEntity("http://localhost:" + port + "/city/" + cityToTest, ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("City not found: " + cityToTest);
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getBody().getPath()).isEqualTo("/city/" + cityToTest);
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 3: Add a new city
    @Test
    public void addCity_Valid(){
        ResponseEntity<CityEntity> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/city/addCity", CityEntity
                        .builder()
                        .name("TestCity")
                        .lat(validLat)
                        .lon(validLon)
                        .build(), CityEntity.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("TestCity");
        assertThat(response.getBody().getLat()).isEqualTo(59.3294);
        assertThat(response.getBody().getLon()).isEqualTo(18.0686);
    }

    // Test Case 4: Add a new city with a name that already exists
    @Test
    public void addCityWithExistingName(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/city/addCity", CityEntity
                        .builder()
                        .name("Stockholm")
                        .lat(validLat)
                        .lon(validLon)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("City already exists: Stockholm");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/city/addCity");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 5: Add a new city with a name that is null
    @Test
    public void addCityWithNullName(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/city/addCity", CityEntity
                        .builder()
                        .name(null)
                        .lat(validLat)
                        .lon(validLon)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("City name cannot be null or empty");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/city/addCity");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 6: Add a new city with a name that is empty
    @Test
    public void addCityWithEmptyName(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/city/addCity", CityEntity
                        .builder()
                        .name("")
                        .lat(validLat)
                        .lon(validLon)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("City name cannot be null or empty");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/city/addCity");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 7: Add a new city with a Lat that is wrong
    @Test
    public void addCityWithWrongLat(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/city/addCity", CityEntity
                        .builder()
                        .name("TestCity")
                        .lat(91.0)
                        .lon(validLon)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Invalid value: 91.0, Latitude must be between 55 and 71");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/city/addCity");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 8: Add a new city with a Lon that is wrong
    @Test
    public void addCityWithWrongLon(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/city/addCity", CityEntity
                        .builder()
                        .name("TestCity")
                        .lon(181.0)
                        .lat(validLat)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Invalid value: 181.0, Longitude must be between 4 and 32");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/city/addCity");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 9: Add a new city without a Lon
    @Test
    public void addCityWithoutLon(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/city/addCity", CityEntity
                        .builder()
                        .name("TestCity")
                        .lat(validLat)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Longitude cannot be null");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/city/addCity");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 10: Add a new city without a Lat
    @Test
    public void addCityWithoutLat(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/city/addCity", CityEntity
                        .builder()
                        .name("TestCity")
                        .lon(validLon)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Latitude cannot be null");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/city/addCity");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 11: Add a new city without a name
    @Test
    public void addCityWithoutName(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/city/addCity", CityEntity
                        .builder()
                        .lat(validLat)
                        .lon(validLon)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("City name cannot be null or empty");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/city/addCity");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 12: Add a new city without a name, lat and lon
    @Test
    public void addCityWithoutNameLatLon(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/city/addCity", CityEntity
                        .builder()
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError())
                .isEqualTo("City name cannot be null or empty, Longitude cannot be null, Latitude cannot be null");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/city/addCity");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 13: Get a city with case-insensitive name
    @Test
    public void retrieveCityTestValidCaseInsensitive() {
        ResponseEntity<CityEntity> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .getForEntity("http://localhost:" + port + "/city/STockHolM", CityEntity.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getName()).isEqualTo("Stockholm");
        assertThat(response.getBody().getLat()).isEqualTo(59.3294);
        assertThat(response.getBody().getLon()).isEqualTo(18.0686);
    }

    // Test Case 14: Try to add a city without sending a body
    @Test
    public void addCityWithoutBody(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/city/addCity", null, ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError())
                .isEqualTo("Unsupported Media Type: Content-Type 'application/x-www-form-urlencoded;charset=UTF-8' is not supported");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
        assertThat(response.getBody().getPath()).isEqualTo("/city/addCity");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 15: Try to add a city with a wrong body type
    @Test
    public void addCityWithWrongContentType(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/city/addCity",
                        new AuthEntity(10,
                                "test",
                                "test",
                                UserRole.USER), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError())
                .isEqualTo("City name cannot be null or empty, Longitude cannot be null, Latitude cannot be null");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/city/addCity");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 16: Try to add a city with lon as a string
    @Test
    public void addCityWithLonAsString(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestJson = "{\"name\":\"TestCity\", \"lat\":\"1.0\", \"lon\":\"invalidLon\"}";
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/city/addCity",
                        new HttpEntity<>(requestJson, headers), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError())
                .isEqualTo("Request body is missing or not readable");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/city/addCity");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 17: Try to add a city with lat as a string
    @Test
    public void addCityWithLatAsString(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestJson = "{\"name\":\"TestCity\", \"lat\":\"invalidLat\", \"lon\":\"1.0\"}";
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/city/addCity",
                        new HttpEntity<>(requestJson, headers), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError())
                .isEqualTo("Request body is missing or not readable");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/city/addCity");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }
}
