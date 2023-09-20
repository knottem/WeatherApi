package com.example.weatherapi.services;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.entities.CityEntity;

import java.util.List;

public interface CityService {
    CityEntity getCityByName(String name);
    CityEntity addCity(City city);

    List<CityEntity> getAllCities();
}
