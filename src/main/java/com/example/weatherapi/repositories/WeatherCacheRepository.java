package com.example.weatherapi.repositories;

import com.example.weatherapi.domain.entities.WeatherCacheEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WeatherCacheRepository extends JpaRepository<WeatherCacheEntity, Integer> {

    void deleteByCacheKey(String key);
    Optional<WeatherCacheEntity> findByCacheKey(String key);

    @Query("SELECT w FROM WeatherCacheEntity w WHERE w.cacheKey = :cacheKey ORDER BY w.timestamp DESC LIMIT 1")
    Optional<WeatherCacheEntity> findLatestByCacheKey(@Param("cacheKey") String cacheKey);

}
