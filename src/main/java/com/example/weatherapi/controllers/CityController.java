package com.example.weatherapi.controllers;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.entities.CityEntity;
import com.example.weatherapi.services.CityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CityController {

    private final CityService cityService;

    public CityController(final CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping(path = "/city/{name}")
    public CityEntity retrieveCity(@PathVariable final String name) {
        return cityService.getCityByName(name);
    }

    @PostMapping(path = "/city/create")
    public ResponseEntity<CityEntity> addCity(@Valid @RequestBody City city) {
        return new ResponseEntity<>(cityService.addCity(city), HttpStatus.CREATED);
    }

    @GetMapping(path = "/city/all")
    public List<CityEntity> retrieveAllCities() {
        return cityService.getAllCities();
    }

    @DeleteMapping(path = "/city/delete/{name}")
    public ResponseEntity<String> deleteCity(@PathVariable final String name) {
        return new ResponseEntity<>(cityService.deleteCity(name), HttpStatus.OK);
    }

    @GetMapping(path = "/city/names")
    public List<String> retrieveAllCityNames() {
        return cityService.getAllCityNames();
    }
}
