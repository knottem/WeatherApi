package com.example.weatherapi.services;

import com.example.weatherapi.domain.Auth;
import com.example.weatherapi.domain.entities.AuthEntity;

import java.util.List;

public interface AuthService {

    List<AuthEntity> getAllUsers();

    AuthEntity getUser(String username);

    AuthEntity addUser(Auth auth);
}
