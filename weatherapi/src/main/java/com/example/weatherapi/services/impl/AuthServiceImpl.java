package com.example.weatherapi.services.impl;

import com.example.weatherapi.domain.entities.AuthEntity;
import com.example.weatherapi.exceptions.exceptions.UserNotFoundException;
import com.example.weatherapi.repositories.AuthRepository;
import com.example.weatherapi.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;

    @Autowired
    public AuthServiceImpl(final AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    @Override
    public List<AuthEntity> getAllUsers() {
        return authRepository.findAll();
    }

    @Override
    public AuthEntity getUser(String username) {
        return authRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }


}
