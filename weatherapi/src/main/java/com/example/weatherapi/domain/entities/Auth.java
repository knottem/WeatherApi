package com.example.weatherapi.domain.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Auth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String username;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role {
        USER,
        ADMIN,
        SUPERUSER
    }
}


