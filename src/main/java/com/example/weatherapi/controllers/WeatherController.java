package com.example.weatherapi.controllers;


import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.exceptions.ApiConnectionException;
import com.example.weatherapi.exceptions.CityNotFoundException;
import com.example.weatherapi.services.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller that handles the requests to the api, redirects the requests to the service layer
 * <p>
 * @author Erik Wallenius
 * @see <a href="https://github.com/knottem/WeatherApi">Repository Link</a>
 */
@RestController
@RequestMapping(path = "/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(final WeatherService weatherService){
        this.weatherService = weatherService;
    }

    /**
     * Returns the weather for a city from both the FMI, SMHI and YR api merged together
     * <p>
     * Example request: GET /v1/weather/stockholm
     * @param city the city to get the weather for from the FMI, SMHI and YR api
     * @return the weather for the city from both the FMI, SMHI and YR api merged together
     * @throws ApiConnectionException if all the api's are down
     * @throws CityNotFoundException if the city is not found in the database
     */
    @GetMapping(path = "/{city}")
    public ResponseEntity<Weather> getWeatherMerged(@PathVariable final String city){
        return weatherService.fetchWeatherMergedResponse(city);
    }


    /**
     * Returns the weather for a city from the api's specified in the list
     * <p>
     * Example request: GET /v1/weather/stockholm?apis=fmi,smhi
     * @param city the city to get the weather for from the api's specified in the list
     * @param apis the list of api's to get the weather from
     * @return the weather for the city from the api's specified in the list
     * @throws ApiConnectionException if all the api's are down
     * @throws CityNotFoundException if the city is not found in the database
     */
    @GetMapping(path = "/{city}", params = "apis")
    public ResponseEntity<Weather> getWeatherMergedCustomApis(@PathVariable final String city, final String[] apis){
        return weatherService.fetchWeatherMergedCustomApisResponse(city, List.of(apis));
    }

}
