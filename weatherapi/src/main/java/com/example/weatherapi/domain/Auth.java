package com.example.weatherapi.domain;

import jakarta.persistence.*;
import lombok.Data;

    @Entity
    @Data
    public class Auth {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private int id;
        private String username;
        private String password;
        private String role;
    }
