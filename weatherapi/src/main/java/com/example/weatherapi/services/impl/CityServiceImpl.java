package com.example.weatherapi.services.impl;

import com.example.weatherapi.api.SmhiApi;
import com.example.weatherapi.domain.City;
import com.example.weatherapi.exceptions.CityNotFoundException;
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
    public City getCityByName(String name) {
        Optional<City> City = cityRepository.findByName(name);
        if (City.isPresent()) {
            logger.info("City found: " + City.get().getName());
            return City.get();
        } else {
            logger.error("City not found: " + name);
            throw new CityNotFoundException("City not found: " + name);
        }
    }
}
