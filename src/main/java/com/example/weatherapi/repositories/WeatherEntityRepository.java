package com.example.weatherapi.repositories;

import com.example.weatherapi.domain.entities.WeatherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WeatherEntityRepository extends JpaRepository<WeatherEntity, Integer> {

    @Query("SELECT w FROM WeatherEntity w INNER JOIN w.city c WHERE c.name = :cityName ORDER BY w.timeStamp DESC LIMIT 1")
    Optional<WeatherEntity> findLatestByCityName(@Param("cityName") String cityName);

}
