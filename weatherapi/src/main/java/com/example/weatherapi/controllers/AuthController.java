package com.example.weatherapi.controllers;

import com.example.weatherapi.domain.entities.AuthEntity;
import com.example.weatherapi.repositories.AuthRepository;
import com.example.weatherapi.services.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
