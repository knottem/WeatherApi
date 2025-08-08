package com.example.weatherapi.controllers;

import com.example.weatherapi.domain.Auth;
import com.example.weatherapi.domain.entities.AuthEntity;
import com.example.weatherapi.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(final AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Get all Users" ,
            description = "Returns the list of all users in the system. " +
                    "This endpoint is used for administrative purposes to manage user accounts."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user data", content = {
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AuthEntity.class)))
            }),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content =
                    @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping(path = "/auth/all")
    public List<AuthEntity> retrieveAllUsers() {
        return authService.getAllUsers();
    }

    @Operation(
            summary = "Get a User by Username",
            description = "Returns the user data for a specific username. " +
                    "This endpoint is used to retrieve user information based on their username."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user data", content = {
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthEntity.class))
            }),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "No User was found", content =
                    @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content =
                    @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping(path = "/auth")
    public AuthEntity retrieveUser(@RequestParam final String username) {
        return authService.getUser(username);
    }


    @Operation(
            summary = "Add a new User",
            description = "Creates a new user in the system. " +
                    "This endpoint is used to register new users with their credentials."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created user", content = {
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthEntity.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid input data or User already exists", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content =
                    @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping(path = "/auth/adduser")
    public ResponseEntity<AuthEntity> addUser(@Valid @RequestBody Auth auth) {
        return new ResponseEntity<>(authService.addUser(auth), HttpStatus.CREATED);
    }
}
