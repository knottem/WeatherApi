package com.example.weatherapi.repositories;

import com.example.weatherapi.domain.entities.CityEntity;
import com.example.weatherapi.domain.entities.LatestWeatherApiEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LatestWeatherApiRepository extends JpaRepository<LatestWeatherApiEntity, UUID> {

    Optional<LatestWeatherApiEntity> findByCityAndSmhiAndYrAndFmi(CityEntity city, boolean smhi, boolean yr, boolean fmi);

}
