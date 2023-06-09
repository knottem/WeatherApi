package com.example.weatherapi.controllers;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.services.CityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CityController {

    private final CityService cityService;

    public CityController(final CityService cityService){
        this.cityService = cityService;
    }

    @GetMapping(path = "/city/{name}")
    public City retrieveCity(@PathVariable final String name){
        return cityService.getCityByName(name);
    }
}
