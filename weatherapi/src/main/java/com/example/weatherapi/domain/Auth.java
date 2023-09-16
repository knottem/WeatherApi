package com.example.weatherapi.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Auth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String username;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;

    private enum Role {
        USER,
        ADMIN,
        SUPERUSER
    }
}


