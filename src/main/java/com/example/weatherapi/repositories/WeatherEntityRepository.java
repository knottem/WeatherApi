package com.example.weatherapi.repositories;

import com.example.weatherapi.domain.entities.WeatherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherEntityRepository extends JpaRepository<WeatherEntity, Integer> {


}
