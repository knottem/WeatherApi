package com.example.weatherapi.services.impl;

import com.example.weatherapi.domain.entities.CityEntity;
import com.example.weatherapi.domain.City;
import com.example.weatherapi.exceptions.CityNotFoundException;
import com.example.weatherapi.exceptions.InvalidCityException;
import com.example.weatherapi.repositories.CityRepository;
import com.example.weatherapi.services.CityService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.example.weatherapi.util.CityMapper.toEntity;

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
        CityEntity citySaved = cityRepository.save(toEntity(city));
        logger.info("A new city has been created: {}", citySaved);
        return citySaved;
    }

    // Return all cities
    @Override
    public List<CityEntity> getAllCities() {
        return cityRepository.findAll();
    }

    // Delete a city by name
    @Override
    @Transactional
    public String deleteCity(String name) {
        // Try to get the city by name
        Optional<CityEntity> cityOptional = cityRepository.findByNameIgnoreCase(name);

        // If the city is not found, throw an exception
        if(cityOptional.isEmpty()) {
            throw new CityNotFoundException("City not found: " + name);
        }

        // Otherwise, delete the city and return a message with the deleted city's name
        CityEntity deletedCity = cityOptional.get();
        cityRepository.deleteByNameIgnoreCase(name);
        logger.info("City has been deleted: {}", deletedCity);

        return "City '" + deletedCity.getName() + "' deleted successfully";
    }

    @Override
    public List<String> getAllCityNames() {
        return cityRepository.findAllCityNames();
    }
}
