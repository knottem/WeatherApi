package com.example.weatherapi.util;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.entities.CityEntity;

public class CityMapper {

    public static City toModel(CityEntity entity) {
        return City.builder()
                .name(entity.getName())
                .lat(entity.getLat())
                .lon(entity.getLon())
                .build();
    }

    public static CityEntity toEntity(City city) {
        return CityEntity.builder()
                .name(city.getName())
                .lat(city.getLat())
                .lon(city.getLon())
                .build();
    }
}