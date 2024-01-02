package com.example.weatherapi.repositories;

import com.example.weatherapi.domain.entities.WeatherDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherDataEntity, Long> {
}
