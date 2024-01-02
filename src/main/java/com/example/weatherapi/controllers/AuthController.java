package com.example.weatherapi.controllers;

import com.example.weatherapi.domain.Auth;
import com.example.weatherapi.domain.entities.AuthEntity;
import com.example.weatherapi.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(final AuthService authService) {
        this.authService = authService;
    }

    @GetMapping(path = "/auth/all")
    public List<AuthEntity> retrieveAllUsers() {
        return authService.getAllUsers();
    }

    @GetMapping(path = "/auth")
    public AuthEntity retrieveUser(@RequestParam final String username) {
        return authService.getUser(username);
    }

    @PostMapping(path = "/auth/adduser")
    public ResponseEntity<AuthEntity> addUser(@Valid @RequestBody Auth auth) {
        return new ResponseEntity<>(authService.addUser(auth), HttpStatus.CREATED);
    }
}
