package com.example.weatherapi.repositories;

import com.example.weatherapi.domain.entities.WeatherCacheEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WeatherCacheRepository extends JpaRepository<WeatherCacheEntity, Integer> {

    void deleteByCacheKey(String key);
    Optional<WeatherCacheEntity> findByCacheKey(String key);

}
