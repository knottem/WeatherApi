package com.example.weatherapi.services.impl;

import com.example.weatherapi.domain.Auth;
import com.example.weatherapi.domain.entities.AuthEntity;
import com.example.weatherapi.exceptions.exceptions.UserAlreadyExistsException;
import com.example.weatherapi.exceptions.exceptions.UserNotFoundException;
import com.example.weatherapi.repositories.AuthRepository;
import com.example.weatherapi.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.weatherapi.util.AuthMapper.toEntity;
import static com.example.weatherapi.util.AuthMapper.toModel;

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

    @Override
    public AuthEntity addUser(Auth auth) {
        if(authRepository.findByUsernameIgnoreCase(auth.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists: " + auth.getUsername());
        }
        return authRepository.save(toEntity(auth));
    }


}
