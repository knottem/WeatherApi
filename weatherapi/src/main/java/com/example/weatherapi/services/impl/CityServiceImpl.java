package com.example.weatherapi.services.impl;

import com.example.weatherapi.domain.CityEntity;
import com.example.weatherapi.domain.City;
import com.example.weatherapi.exceptions.exceptions.CityNotFoundException;
import com.example.weatherapi.exceptions.exceptions.InvalidCityException;
import com.example.weatherapi.exceptions.handlers.CustomExceptionHandler;
import com.example.weatherapi.repositories.CityRepository;
import com.example.weatherapi.services.CityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
        if(city.getName() == null || city.getName().isEmpty()) {
            throw new InvalidCityException("City name cannot be null or empty");
        }
        if(city.getLon() == null) {
            throw new InvalidCityException("Longitude cannot be null");
        }
        if(city.getLat() == null) {
            throw new InvalidCityException("Latitude cannot be null");
        }

        if(city.getLon() < -180 || city.getLon() > 180) {
            throw new InvalidCityException("Invalid longitude value: " + city.getLon() + ", must be between -180 and 180");
        }

        if(city.getLat() < -90 || city.getLat() > 90) {
            throw new InvalidCityException("Invalid latitude value: " + city.getLat() + ", must be between -90 and 90");
        }

        Optional<CityEntity> existingCity = cityRepository.findByNameIgnoreCase(city.getName());
        if(existingCity.isPresent()) {
            throw new InvalidCityException("City already exists: " + city.getName());
        }
        CityEntity cityEntity = new CityEntity();
        cityEntity.setName(city.getName());
        cityEntity.setLon(city.getLon());
        cityEntity.setLat(city.getLat());

        CityEntity citySaved = cityRepository.save(cityEntity);
        logger.info("A new city has been created: {}", citySaved);
        return citySaved;
    }
}
