package com.example.weatherapi.services.impl;

import com.example.weatherapi.domain.dto.CityDto;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
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
        Optional<CityEntity> cityOptional = cityRepository.findByNameIgnoreCase(name);
        if(cityOptional.isEmpty()) {
            throw new CityNotFoundException("City not found: " + name);
        }
        CityEntity deletedCity = cityOptional.get();
        cityRepository.deleteByNameIgnoreCase(name);
        logger.info("City has been deleted: {}", deletedCity);
        return "City '" + deletedCity.getName() + "' deleted successfully";
    }

    @Override
    @Cacheable("cache")
    public List<CityDto> getAllCityNames() {
        return cityRepository.findAllCityNames().stream()
                .filter(result -> result[0] != null) // Filter out null values (Should not happen since city names are mandatory)
                .map(result -> CityDto.builder()
                        .name((String) result[0])
                        .en(result[1] != null ? (String) result[1] : null) // English name
                        .build())
                .sorted(Comparator.comparing(CityDto::getName)) // Sort names alphabetically
                .toList();
    }

}
