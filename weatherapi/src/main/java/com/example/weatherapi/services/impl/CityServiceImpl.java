package com.example.weatherapi.services.impl;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.exceptions.CityNotFoundException;
import com.example.weatherapi.repositories.CityRepository;
import com.example.weatherapi.services.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;

    @Autowired
    public CityServiceImpl(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @Override
    public City getCityByName(String name) {
        return cityRepository.findByName(name)
                .orElseThrow(() -> new CityNotFoundException("City not found: " + name));
    }
}
