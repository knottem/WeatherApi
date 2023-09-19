package com.example.weatherapi.services.impl;

import com.example.weatherapi.domain.entities.CityEntity;
import com.example.weatherapi.domain.City;
import com.example.weatherapi.exceptions.exceptions.CityNotFoundException;
import com.example.weatherapi.exceptions.exceptions.InvalidCityException;
import com.example.weatherapi.repositories.CityRepository;
import com.example.weatherapi.services.CityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;

    private final Logger logger = LoggerFactory.getLogger(CityServiceImpl.class);

    @Autowired
    public CityServiceImpl(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @Override
    public CityEntity getCityByName(String name) {
        return cityRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new CityNotFoundException("City not found: " + name));
    }

    @Override
    public CityEntity addCity(City city) {
        if(cityRepository.findByNameIgnoreCase(city.getName()).isPresent()) {
            throw new InvalidCityException("City already exists: " + city.getName());
        }
        CityEntity citySaved = cityRepository.save(CityEntity.builder()
                .name(city.getName())
                .lon(city.getLon())
                .lat(city.getLat())
                .build());
        logger.info("A new city has been created: {}", citySaved);
        return citySaved;
    }

    @Override
    public List<CityEntity> getAllCities() {
        return cityRepository.findAll();
    }
}
