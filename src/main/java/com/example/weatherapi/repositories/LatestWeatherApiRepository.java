package com.example.weatherapi.repositories;

import com.example.weatherapi.domain.entities.CityEntity;
import com.example.weatherapi.domain.entities.LatestWeatherApiEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LatestWeatherApiRepository extends JpaRepository<LatestWeatherApiEntity, UUID> {

    Optional<LatestWeatherApiEntity> findByCityAndSmhiAndYrAndFmi(CityEntity city, boolean smhi, boolean yr, boolean fmi);

    @Query("""
    SELECT lwa
    FROM LatestWeatherApiEntity lwa
    JOIN FETCH lwa.latestWeather lw
    JOIN FETCH lw.weatherDataList wd
    WHERE lw.timeStamp > :validThreshold
    """)
    List<LatestWeatherApiEntity> findValidWeatherData(@Param("validThreshold") ZonedDateTime validThreshold);


}
