package com.example.weatherapi.repositories;

import com.example.weatherapi.domain.entities.AuthEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<AuthEntity, Integer> {
    Optional<AuthEntity> findByUsername(String username);
    Optional<AuthEntity> findByUsernameIgnoreCase(String username);
}
