package com.example.weatherapi.controllers;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.CityEntity;
import com.example.weatherapi.services.CityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping(path = "/city/addCity")
    public ResponseEntity<CityEntity> addCity(@RequestBody City city) {
        return new ResponseEntity<>(cityService.addCity(city), HttpStatus.CREATED);
    }
}
