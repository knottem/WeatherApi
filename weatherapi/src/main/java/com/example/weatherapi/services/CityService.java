package com.example.weatherapi.services;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.CityEntity;

public interface CityService {
    CityEntity getCityByName(String name);
    CityEntity addCity(City city);
}
