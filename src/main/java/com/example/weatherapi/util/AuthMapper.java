package com.example.weatherapi.util;

import com.example.weatherapi.domain.Auth;
import com.example.weatherapi.domain.entities.AuthEntity;

/**
 * This class contains methods for converting between Auth and AuthEntity objects.
 * @author Erik Wallenius
 * @see <a href="https://github.com/knottem/WeatherApi">Repository Link</a>
 */
public class AuthMapper {

    /**
     * Convert an AuthEntity object to an Auth object.
     * @param entity the AuthEntity object to convert
     * @return the converted Auth object
     */
    public static Auth toModel(AuthEntity entity) {
        return Auth.builder()
                .username(entity.getUsername())
                .password(entity.getPassword())
                .role(entity.getRole())
                .build();
    }

    /**
     * Convert an Auth object to an AuthEntity object.
     * @param auth the Auth object to convert
     * @return the converted AuthEntity object
     */
    public static AuthEntity toEntity(Auth auth) {
        return AuthEntity.builder()
                .username(auth.getUsername())
                .password(auth.getPassword())
                .role(auth.getRole())
                .build();
    }
}
