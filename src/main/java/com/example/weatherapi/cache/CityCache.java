package com.example.weatherapi.cache;

import com.example.weatherapi.domain.dto.CityDto;
import com.example.weatherapi.domain.entities.CityEntity;
import com.example.weatherapi.repositories.CityRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class CityCache {

    private final Logger log;
    private final Cache<String, List<CityEntity>> cityEntityCache;
    private final Cache<String, List<CityDto>> cityDtoCache;
    private final CityRepository cityRepository;

    @Autowired
    public CityCache(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
        this.log = LoggerFactory.getLogger(CityCache.class);
        this.cityEntityCache = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES).build();
        this.cityDtoCache = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES).build();
    }

    public List<CityEntity> getAllCities() {
        return cityEntityCache.get("cities", key -> {
            log.debug("Fetching cities from the database");
            return cityRepository.findAll().stream()
                    .sorted(Comparator.comparing(CityEntity::getName))
                    .toList();
        });
    }

    public List<CityDto> getAllCitiesDto() {
        return cityDtoCache.get("cities", key -> {
            log.debug("Updating cache for citiesDto List");
            return getAllCities().stream()
                    .map(cityEntity -> CityDto.builder()
                            .name(cityEntity.getName())
                            .en(cityEntity.getName_en())
                            .build())
                    .toList();
        });
    }

}
