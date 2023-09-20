package com.example.weatherapi.util;

import com.example.weatherapi.domain.Auth;
import com.example.weatherapi.domain.entities.AuthEntity;

public class AuthMapper {

    public static Auth toModel(AuthEntity entity) {
        return Auth.builder()
                .username(entity.getUsername())
                .password(entity.getPassword())
                .role(entity.getRole())
                .build();
    }

    public static AuthEntity toEntity(Auth auth) {
        return AuthEntity.builder()
                .username(auth.getUsername())
                .password(auth.getPassword())
                .role(auth.getRole())
                .build();
    }
}
