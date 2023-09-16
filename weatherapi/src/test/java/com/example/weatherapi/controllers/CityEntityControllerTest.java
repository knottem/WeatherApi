package com.example.weatherapi.controllers;

import com.example.weatherapi.domain.CityEntity;
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
public class CityEntityControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

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
    public void addCity(){
        ResponseEntity<CityEntity> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/addCity", CityEntity
                        .builder()
                        .name("TestCity")
                        .lat(1.0)
                        .lon(1.0)
                        .build(), CityEntity.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(9L);
        assertThat(response.getBody().getName()).isEqualTo("TestCity");
        assertThat(response.getBody().getLat()).isEqualTo(1.0);
        assertThat(response.getBody().getLon()).isEqualTo(1.0);
    }

    // Test Case 4: Add a new city with a name that already exists
    @Test
    public void addCityWithExistingName(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/addCity", CityEntity
                        .builder()
                        .name("Stockholm")
                        .lat(1.0)
                        .lon(1.0)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("City already exists: Stockholm");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/addCity");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 5: Add a new city with a name that is null
    @Test
    public void addCityWithNullName(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/addCity", CityEntity
                        .builder()
                        .name(null)
                        .lat(1.0)
                        .lon(1.0)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("City name cannot be null or empty");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/addCity");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 6: Add a new city with a name that is empty
    @Test
    public void addCityWithEmptyName(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/addCity", CityEntity
                        .builder()
                        .name("")
                        .lat(1.0)
                        .lon(1.0)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("City name cannot be null or empty");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/addCity");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 7: Add a new city with a Lat that is wrong
    @Test
    public void addCityWithWrongLat(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/addCity", CityEntity
                        .builder()
                        .name("TestCity")
                        .lon(1.0)
                        .lat(91.0)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Invalid latitude value: 91.0, must be between -90 and 90");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/addCity");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 8: Add a new city with a Lon that is wrong
    @Test
    public void addCityWithWrongLon(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/addCity", CityEntity
                        .builder()
                        .name("TestCity")
                        .lon(181.0)
                        .lat(1.0)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Invalid longitude value: 181.0, must be between -180 and 180");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/addCity");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 9: Add a new city without a Lon
    @Test
    public void addCityWithoutLon(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/addCity", CityEntity
                        .builder()
                        .name("TestCity")
                        .lat(1.0)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Longitude cannot be null");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/addCity");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 10: Add a new city without a Lat
    @Test
    public void addCityWithoutLat(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/addCity", CityEntity
                        .builder()
                        .name("TestCity")
                        .lon(1.0)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Latitude cannot be null");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/addCity");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 11: Add a new city without a name
    @Test
    public void addCityWithoutName(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/addCity", CityEntity
                        .builder()
                        .lat(1.0)
                        .lon(1.0)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("City name cannot be null or empty");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/addCity");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 12: Add a new city without a name, lat and lon
    @Test
    public void addCityWithoutNameLatLon(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/addCity", CityEntity
                        .builder()
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("City name cannot be null or empty");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/addCity");
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

}
