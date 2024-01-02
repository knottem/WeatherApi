package com.example.weatherapi.controllers;

import com.example.weatherapi.domain.Auth;
import com.example.weatherapi.domain.ErrorResponse;
import com.example.weatherapi.domain.UserRole;
import com.example.weatherapi.domain.entities.AuthEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class AuthEntityControllerTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private final String endpoint = "/api/v1/auth";
    private final String baseUrl = "http://localhost:";

    // Test Case 1: Forbidden request to auth by a user that is not admin
    @Test
    void getAuthListByUserTest_Forbidden() {
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("user", "pass123")
                .getForEntity(baseUrl + port + endpoint + "/all", ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Forbidden");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/all");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 2: List of all users
    @Test
    void getAuthListByAdminTest_OK() throws JsonProcessingException {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .getForEntity(baseUrl + port + endpoint + "/all", String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonResponse = mapper.readTree(response.getBody());

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    // Test Case 3: Retrieve specific user with role ADMIN
    @Test
    void getAuthAdminByAdminTest_OK() {
        ResponseEntity<AuthEntity> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .getForEntity(baseUrl + port + endpoint + "?username=admin", AuthEntity.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("admin");
        assertThat(response.getBody().getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(response.getBody().getPassword()).isNull();

    }

    // Test Case 4: Retrieve specific user with role USER
    @Test
    void getAuthUserByAdminTest_OK() {
        ResponseEntity<AuthEntity> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .getForEntity(baseUrl + port + endpoint + "?username=user", AuthEntity.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("user");
        assertThat(response.getBody().getRole()).isEqualTo(UserRole.USER);
        assertThat(response.getBody().getPassword()).isNull();
    }

    // Test Case 5: Try to retrieve a user that does not exist
    @Test
    void getAuthByAdminTest_NotFound() {
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .getForEntity(baseUrl + port + endpoint + "?username=doesnotexist", ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("User not found with username: doesnotexist");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getBody().getPath()).isEqualTo(endpoint);
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 6: Add a new user with Valid credentials
    @Test
    void addAuthByAdminTest_OK() {
        Auth auth = Auth.builder()
                .username("testing")
                .password("Pass1234")
                .role(UserRole.USER)
                .build();

        ResponseEntity<Auth> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity(baseUrl + port + endpoint + "/adduser", auth, Auth.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("testing");
        assertThat(response.getBody().getRole()).isEqualTo(UserRole.USER);
        assertThat(response.getBody().getPassword()).isNull();
    }

    // Test Case 7: Add a new user with too short username
    @Test
    void addAuthByAdminTest_BadRequestShortUsername() {
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity(baseUrl + port + endpoint + "/adduser", Auth.builder()
                        .username("te")
                        .password("Pass1234")
                        .role(UserRole.USER)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Invalid value: te, Username must be between 4 and 20 characters long");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/adduser");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 8: Add a new user with too long username
    @Test
    void addAuthByAdminTest_BadRequestLongUsername() {
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity(baseUrl + port + endpoint + "/adduser", Auth.builder()
                        .username("testtesttesttesttesttesttesttest")
                        .password("Pass1234")
                        .role(UserRole.USER)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Invalid value: testtesttesttesttesttesttesttest, Username must be between 4 and 20 characters long");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/adduser");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 9: Add a new user with too short password
    @Test
    void addAuthByAdminTest_BadRequestShortPassword() {
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity(baseUrl + port + endpoint + "/adduser", Auth.builder()
                        .username("test")
                        .password("Pass123")
                        .role(UserRole.USER)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Password must be between 8 and 30 characters long");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/adduser");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 10: Add a new user with too long password
    @Test
    void addAuthByAdminTest_BadRequestLongPassword() {
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity(baseUrl + port + endpoint + "/adduser", Auth.builder()
                        .username("test")
                        .password("Pass123456789012345678901234567890")
                        .role(UserRole.USER)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Password must be between 8 and 30 characters long");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/adduser");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 11: Add a new user with no uppercase letter in password
    @Test
    void addAuthByAdminTest_BadRequestNoUppercasePassword() {
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity(baseUrl + port + endpoint + "/adduser", Auth.builder()
                        .username("test")
                        .password("pass1234")
                        .role(UserRole.USER)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Password must contain at least one uppercase letter, one lowercase letter, and one digit");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/adduser");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 12: Add a new user with no lowercase letter in password
    @Test
    void addAuthByAdminTest_BadRequestNoLowercasePassword() {
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity(baseUrl + port + endpoint + "/adduser", Auth.builder()
                        .username("test")
                        .password("PASS1234")
                        .role(UserRole.USER)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Password must contain at least one uppercase letter, one lowercase letter, and one digit");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/adduser");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 13: Add a new user with no digit in password
    @Test
    void addAuthByAdminTest_BadRequestNoDigitPassword() {
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity(baseUrl + port + endpoint + "/adduser", Auth.builder()
                        .username("test")
                        .password("PassWord")
                        .role(UserRole.USER)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Password must contain at least one uppercase letter, one lowercase letter, and one digit");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/adduser");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 14: Add a new user with no role
    @Test
    void addAuthByAdminTest_BadRequestNoRole() {
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity(baseUrl + port + endpoint + "/adduser", Auth.builder()
                        .username("test")
                        .password("Pass1234")
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Role cannot be null");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/adduser");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 15: Add a new user with no username
    @Test
    void addAuthByAdminTest_BadRequestNoUsername() {
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity(baseUrl + port + endpoint + "/adduser", Auth.builder()
                        .password("Pass1234")
                        .role(UserRole.USER)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Username cannot be null or empty");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/adduser");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 16: Add a new user with no password
    @Test
    void addAuthByAdminTest_BadRequestNoPassword() {
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity(baseUrl + port + endpoint + "/adduser", Auth.builder()
                        .username("test")
                        .role(UserRole.USER)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Password cannot be null or empty");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/adduser");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 17: Add a new user with username that already exists
    @Test
    void addAuthByAdminTest_Conflict() {
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity(baseUrl + port + endpoint + "/adduser", Auth.builder()
                        .username("admin")
                        .password("Pass1234")
                        .role(UserRole.USER)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("User already exists: admin");
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/adduser");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    // Test Case 18: Add a new user with error in both username and password
    @Test
    void addAuthByAdminTest_BadRequestBothUsernameAndPassword() {
        ResponseEntity<ErrorResponse> response = restTemplate
                .withBasicAuth("admin", "pass123")
                .postForEntity(baseUrl + port + endpoint + "/adduser", Auth.builder()
                        .password("Pass123")
                        .username("te")
                        .role(UserRole.USER)
                        .build(), ErrorResponse.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getPath()).isEqualTo(endpoint + "/adduser");
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(OffsetDateTime.now());
        assertThat(response.getBody().getError()).isEqualTo("Password must be between 8 and 30 characters long, Invalid value: te, Username must be between 4 and 20 characters long");
    }

}
