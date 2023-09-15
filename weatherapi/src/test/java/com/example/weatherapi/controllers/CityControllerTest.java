package com.example.weatherapi.controllers;

import com.example.weatherapi.domain.City;
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
public class CityControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    // Test Case 1: retrieve city that exists
    @Test
    public void retrieveCityTestValid() {
        ResponseEntity<City> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .getForEntity("http://localhost:" + port + "/city/Stockholm", City.class);

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

    // Test Case 3: forbidden request to city by a user that is not admin
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

    // Test Case 4: no auth request to city
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

    // Test Case 6: Wrong password
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


    // Test Case 7: Add a new city
    @Test
    public void addCity(){
        City city = new City();
        city.setName("TestCity");
        city.setLat(1.0);
        city.setLon(1.0);
        ResponseEntity<City> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/city", city, City.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(9L);
        assertThat(response.getBody().getName()).isEqualTo("TestCity");
        assertThat(response.getBody().getLat()).isEqualTo(1.0);
        assertThat(response.getBody().getLon()).isEqualTo(1.0);
    }

    // Test Case 8: Add a new city with a name that already exists
    @Test
    public void addCityWithExistingName(){
        City city = new City();
        city.setName("Stockholm");
        city.setLat(1.0);
        city.setLon(1.0);
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/city", city, ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("City already exists: " + city.getName());
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/city");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 9: Add a new city with a name that is null
    @Test
    public void addCityWithNullName(){
        City city = new City();
        city.setName(null);
        city.setLat(1.0);
        city.setLon(1.0);
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/city", city, ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("City name cannot be null");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/city");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 10: Add a new city with a name that is empty
    @Test
    public void addCityWithEmptyName(){
        City city = new City();
        city.setName("");
        city.setLat(1.0);
        city.setLon(1.0);
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/city", city, ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("City name cannot be empty");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/city");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 11: Add a new city with a Lat that is wrong
    @Test
    public void addCityWithWrongLat(){
        City city = new City();
        city.setName("TestCity");
        city.setLat(91.0);
        city.setLon(1.0);
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/city", city, ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Latitude must be between -90 and 90");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/city");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 12: Add a new city with a Lon that is wrong
    @Test
    public void addCityWithWrongLon(){
        City city = new City();
        city.setName("TestCity");
        city.setLat(1.0);
        city.setLon(181.0);
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + "/city", city, ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Longitude must be between -180 and 180");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo("/city");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

}
