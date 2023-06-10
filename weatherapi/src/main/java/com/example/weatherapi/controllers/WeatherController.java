package com.example.weatherapi.controllers;


import com.example.weatherapi.domain.weather.WeatherYr;
import com.example.weatherapi.services.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(final WeatherService weatherService){
        this.weatherService = weatherService;
    }


    @GetMapping(path = "/weather/smhi/{city}")
    public ResponseEntity<Object> getWeatherByCity(@PathVariable final String city){
        return weatherService.getWeatherBySmhiCity(city);
    }

    @GetMapping(path = "/weather/yr/{city}")
    public WeatherYr getWeatherByYrCity(@PathVariable final String city){
        return weatherService.getWeatherByYrCity(city);
    }
}
