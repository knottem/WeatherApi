package com.example.weatherapi.repositories;

import com.example.weatherapi.domain.entities.WeatherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WeatherEntityRepository extends JpaRepository<WeatherEntity, Integer> {

}
