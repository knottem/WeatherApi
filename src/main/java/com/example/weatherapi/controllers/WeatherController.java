package com.example.weatherapi.controllers;


import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.exceptions.ApiConnectionException;
import com.example.weatherapi.exceptions.CityNotFoundException;
import com.example.weatherapi.services.WeatherService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
     * Returns the weather for a city from both the SMHI and YR api merged together
     * <p>
     * Example request: GET /v1/weather/stockholm
     * @param city the city to get the weather for from the SMHI and YR api
     * @return the weather for the city from both the SMHI and YR api merged together
     * @throws ApiConnectionException if the connection to either the SMHI or YR api fails
     * @throws CityNotFoundException if the city is not found in the database
     */
    @GetMapping(path = "/{city}")
    public ResponseEntity<Weather> getWeatherMerged(@PathVariable final String city){
        Weather weather = weatherService.getWeatherMerged(city.toLowerCase());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(weather, headers, HttpStatus.OK);
    }

}
