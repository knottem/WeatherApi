package com.example.weatherapi.controllers;

import com.example.weatherapi.domain.city.City;
import com.example.weatherapi.domain.city.CitySearchRequest;
import com.example.weatherapi.domain.city.CitySearchResponse;
import com.example.weatherapi.domain.dto.CityDto;
import com.example.weatherapi.domain.entities.CityEntity;
import com.example.weatherapi.services.CityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path ="/city")
public class CityController {

    private final CityService cityService;

    public CityController(final CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping(path = "/{name}")
    public CityEntity retrieveCity(@PathVariable final String name) {
        return cityService.getCityByName(name);
    }

    @PostMapping(path = "/create")
    public ResponseEntity<CityEntity> addCity(@Valid @RequestBody City city) {
        return new ResponseEntity<>(cityService.addCity(city), HttpStatus.CREATED);
    }

    @GetMapping(path = "/all")
    public List<CityEntity> retrieveAllCities() {
        return cityService.getAllCities();
    }

    @DeleteMapping(path = "/delete/{name}")
    public ResponseEntity<String> deleteCity(@PathVariable final String name) {
        return new ResponseEntity<>(cityService.deleteCity(name), HttpStatus.OK);
    }

    @GetMapping(path = "/names")
    public List<CityDto> retrieveAllCityNames() {
        return cityService.getAllCityNames();
    }

    @PostMapping(path = "/search")
    public CitySearchResponse searchCity(@Valid @RequestBody CitySearchRequest request){
        return cityService.searchCity(request.lat(), request.lon());
    }

}
