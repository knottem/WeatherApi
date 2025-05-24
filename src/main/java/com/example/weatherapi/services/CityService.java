package com.example.weatherapi.services;

import com.example.weatherapi.domain.city.City;
import com.example.weatherapi.domain.city.CitySearchResponse;
import com.example.weatherapi.domain.dto.CityDto;
import com.example.weatherapi.domain.entities.CityEntity;

import java.util.List;

public interface CityService {
    CityEntity getCityByName(String name);
    CityEntity addCity(City city);
    List<CityEntity> getAllCities();
    String deleteCity(String name);
    List<CityDto> getAllCityNames();
    CitySearchResponse searchCity(double lat, double lon);
}
