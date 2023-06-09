package com.example.weatherapi.services;

import com.example.weatherapi.domain.City;

public interface CityService {
    City getCityByName(String name);
}
