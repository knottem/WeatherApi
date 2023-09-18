package com.example.weatherapi.util;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.entities.CityEntity;

public class CityMapper {

    public static City toModel(CityEntity entity) {
        City city = new City();
        city.setLat(entity.getLat());
        city.setLon(entity.getLon());
        city.setName(entity.getName());
        return city;
    }

    public static CityEntity toEntity(City city) {
        CityEntity entity = new CityEntity();
        entity.setLat(city.getLat());
        entity.setLon(city.getLon());
        entity.setName(city.getName());
        return entity;
    }
}