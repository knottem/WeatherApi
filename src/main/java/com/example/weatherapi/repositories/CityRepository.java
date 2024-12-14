package com.example.weatherapi.repositories;

import com.example.weatherapi.domain.entities.CityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<CityEntity, String> {

    Optional<CityEntity> findByNameIgnoreCase(String name);
    void deleteByNameIgnoreCase(String name);

    @Query("SELECT c.name, c.name_en FROM CityEntity c")
    List<Object[]> findAllCityNames();
}