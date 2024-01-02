package com.example.weatherapi.controllers;

import com.example.weatherapi.domain.UserRole;
import com.example.weatherapi.domain.entities.AuthEntity;
import com.example.weatherapi.domain.entities.CityEntity;
import com.example.weatherapi.domain.ErrorResponse;
import io.restassured.RestAssured;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CityEntityControllerTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    //Valid numbers
    private static final double validLat = 59.3294;
    private static final double validLon = 18.0686;
    private final String endpoint = "/api/v1/city";

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.authentication = RestAssured.basic("admin", "pass123");
    }

    private static org.hamcrest.Matcher<String> isWithinTimestampWindow(int seconds) {
        OffsetDateTime now = OffsetDateTime.now();
        return allOf(
                greaterThanOrEqualTo(ISO_OFFSET_DATE_TIME.format(now.minusSeconds(seconds))),
                lessThanOrEqualTo(ISO_OFFSET_DATE_TIME.format(now.plusSeconds(seconds))
        ));
    }

    // Test Case 1: retrieve city that exists
    @Test
    void shouldRetrieveValidCityInformation() {
        given()
                .when()
                .get(endpoint + "/Stockholm")
                .then()
                .statusCode(200)
                .assertThat()
                .body("id", equalTo("2785714e-0872-4a61-bfb5-76b5baf8911b"))
                .body("name", equalTo("Stockholm"))
                .body("lat", equalTo(59.3294f))
                .body("lon", equalTo(18.0686f));
    }

    // Test Case 2: retrieve city that doesn't exist
    @Test
    void shouldRetrieveCityNotFound() {
        given()
                .when()
                .get(endpoint + "/CITYNOTFOUND")
                .then()
                .statusCode(404)
                .assertThat()
                .body("error", equalTo("City not found: CITYNOTFOUND"))
                .body("status", equalTo(404))
                .body("path", equalTo(endpoint + "/CITYNOTFOUND"))
                .body("timestamp", isWithinTimestampWindow(5));

    }

    // Test Case 3: Add a new city
    @Test
    void addCity_Valid(){
        ResponseEntity<CityEntity> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + endpoint + "/create", CityEntity
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
    void addCityWithExistingName(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + endpoint + "/create", CityEntity
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
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/create");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 5: Add a new city with a name that is null
    @Test
    void addCityWithNullName(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + endpoint + "/create", CityEntity
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
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/create");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 6: Add a new city with a name that is empty
    @Test
    void addCityWithEmptyName(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + endpoint + "/create", CityEntity
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
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/create");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 7: Add a new city with a Lat that is wrong
    @Test
    void addCityWithWrongLat(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + endpoint + "/create", CityEntity
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
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/create");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 8: Add a new city with a Lon that is wrong
    @Test
    void addCityWithWrongLon(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + endpoint + "/create", CityEntity
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
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/create");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 9: Add a new city without a Lon
    @Test
    void addCityWithoutLon(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + endpoint + "/create", CityEntity
                        .builder()
                        .name("TestCity")
                        .lat(validLat)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Longitude cannot be null");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/create");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 10: Add a new city without a Lat
    @Test
    void addCityWithoutLat(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + endpoint + "/create", CityEntity
                        .builder()
                        .name("TestCity")
                        .lon(validLon)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Latitude cannot be null");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/create");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 11: Add a new city without a name
    @Test
    void addCityWithoutName(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + endpoint + "/create", CityEntity
                        .builder()
                        .lat(validLat)
                        .lon(validLon)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("City name cannot be null or empty");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/create");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 12: Add a new city without a name, lat and lon
    @Test
    void addCityWithoutNameLatLon(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + endpoint + "/create", CityEntity
                        .builder()
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError())
                .isEqualTo("City name cannot be null or empty, Longitude cannot be null, Latitude cannot be null");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/create");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 13: Get a city with case-insensitive name
    @Test
    void retrieveCityTestValidCaseInsensitive() {
        ResponseEntity<CityEntity> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .getForEntity("http://localhost:" + port + endpoint + "/STockHolM", CityEntity.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(UUID.fromString("2785714e-0872-4a61-bfb5-76b5baf8911b"));
        assertThat(response.getBody().getName()).isEqualTo("Stockholm");
        assertThat(response.getBody().getLat()).isEqualTo(59.3294);
        assertThat(response.getBody().getLon()).isEqualTo(18.0686);
    }

    // Test Case 14: Try to add a city without sending a body
    @Test
    void addCityWithoutBody(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + endpoint + "/create", null, ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError())
                .isEqualTo("Unsupported Media Type: Content-Type 'application/x-www-form-urlencoded;charset=UTF-8' is not supported");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/create");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 15: Try to add a city with a wrong body type
    @Test
    void addCityWithWrongContentType(){
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + endpoint + "/create",
                        new AuthEntity(UUID.randomUUID(),
                                "test",
                                "test",
                                UserRole.USER), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError())
                .isEqualTo("City name cannot be null or empty, Longitude cannot be null, Latitude cannot be null");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/create");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 16: Try to add a city with lon as a string
    @Test
    void addCityWithLonAsString(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestJson = "{\"name\":\"TestCity\", \"lat\":\"1.0\", \"lon\":\"invalidLon\"}";
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + endpoint + "/create",
                        new HttpEntity<>(requestJson, headers), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError())
                .isEqualTo("Request body is missing or not readable");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/create");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 17: Try to add a city with lat as a string
    @Test
    void addCityWithLatAsString(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestJson = "{\"name\":\"TestCity\", \"lat\":\"invalidLat\", \"lon\":\"1.0\"}";
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity("http://localhost:" + port + endpoint + "/create",
                        new HttpEntity<>(requestJson, headers), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError())
                .isEqualTo("Request body is missing or not readable");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/create");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 18: Get all cities
    @Test
    void retrieveAllCitiesTestValid() {
        ResponseEntity<CityEntity[]> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .getForEntity("http://localhost:" + port + endpoint + "/all", CityEntity[].class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()[0].getId()).isEqualTo(UUID.fromString("2785714e-0872-4a61-bfb5-76b5baf8911b"));
        assertThat(response.getBody()[0].getName()).isEqualTo("Stockholm");
        assertThat(response.getBody()[0].getLat()).isEqualTo(59.3294);
        assertThat(response.getBody()[0].getLon()).isEqualTo(18.0686);
        assertThat(response.getBody()[1].getId()).isEqualTo(UUID.fromString("82f45775-ed0a-44e4-afe6-a07d97e9663c"));
        assertThat(response.getBody()[1].getName()).isEqualTo("Göteborg");
        assertThat(response.getBody()[1].getLat()).isEqualTo(57.7075);
        assertThat(response.getBody()[1].getLon()).isEqualTo(11.9675);
    }

    // Test Case 19: Delete a city that exists
    @Test
    void deleteCityTestValid() {
        String cityToTest = "Norrköping";

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .exchange("http://localhost:" + port + endpoint + "/delete/" + cityToTest, HttpMethod.DELETE, null, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo("City '" + cityToTest + "' deleted successfully");

        // Assert that the city is deleted by trying to retrieve it
        ResponseEntity<ErrorResponse> response2 = restTemplate
                .withBasicAuth("admin", "pass123")
                .getForEntity("http://localhost:" + port + endpoint + "/" + cityToTest, ErrorResponse.class);

        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response2.getBody()).isNotNull();
        assertThat(response2.getBody().getError()).isEqualTo("City not found: " + cityToTest);
        assertThat(response2.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response2.getBody().getPath()).isEqualTo(endpoint + "/" + cityToTest);
        assertThat(response2.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());

    }

    // Test Case 20: Delete a city that doesn't exist
    @Test
    void deleteCityTestFaulty() {
        String cityToTest = "CITYNOTFOUND";

        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .exchange("http://localhost:" + port + endpoint + "/delete/" + cityToTest, HttpMethod.DELETE, null, ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("City not found: " + cityToTest);
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/delete/" + cityToTest);
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 21: Delete a city with case-insensitive name
    @Test
    void deleteCityTestValidCaseInsensitive() {
         ResponseEntity<String> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .exchange("http://localhost:" + port + endpoint + "/delete/UppSAla", HttpMethod.DELETE, null, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo("City 'Uppsala' deleted successfully");

        // Assert that the city is deleted by trying to retrieve it
        ResponseEntity<ErrorResponse> response2 = restTemplate
                .withBasicAuth("admin", "pass123")
                .getForEntity("http://localhost:" + port + endpoint + "/UppSAla" , ErrorResponse.class);

        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response2.getBody()).isNotNull();
        assertThat(response2.getBody().getError()).isEqualTo("City not found: UppSAla");
        assertThat(response2.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response2.getBody().getPath()).isEqualTo(endpoint + "/UppSAla" );
        assertThat(response2.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 22: Delete a city with UTF-8 characters
    @Test
    void deleteCityTestValidUTF8() {
        String cityToTest = "Linköping";

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .exchange("http://localhost:" + port + endpoint + "/delete/" + cityToTest, HttpMethod.DELETE, null, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo("City '" + cityToTest + "' deleted successfully");

        // Assert that the city is deleted by trying to retrieve it
        ResponseEntity<ErrorResponse> response2 = restTemplate
                .withBasicAuth("admin", "pass123")
                .getForEntity("http://localhost:" + port + endpoint + "/" + cityToTest, ErrorResponse.class);

        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response2.getBody()).isNotNull();
        assertThat(response2.getBody().getError()).isEqualTo("City not found: " + cityToTest);
        assertThat(response2.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response2.getBody().getPath()).isEqualTo(endpoint + "/" + cityToTest);
        assertThat(response2.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 23: Get all city names
    @Test
    void retrieveAllCityNamesTestValidAndSortedByAlphabet() {
        ResponseEntity<String[]> response = restTemplate
                .getForEntity("http://localhost:" + port + endpoint + "/names", String[].class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()[0]).isEqualTo("Gävle");
        assertThat(response.getBody()[1]).isEqualTo("Göteborg");
        assertThat(response.getBody()[2]).isEqualTo("Helsingborg");
    }


}
