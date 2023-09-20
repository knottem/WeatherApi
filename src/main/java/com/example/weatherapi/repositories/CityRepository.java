package com.example.weatherapi.repositories;

import com.example.weatherapi.domain.entities.CityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<CityEntity, String> {
    Optional<CityEntity> findByNameIgnoreCase(String name);
}