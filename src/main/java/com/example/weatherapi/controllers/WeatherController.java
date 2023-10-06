package com.example.weatherapi.controllers;


import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.services.WeatherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
/**
 * Controller that handles the requests to the api, redirects the requests to the service layer
 * <p>
 * @author Erik Wallenius
 * @see <a href="https://github.com/knottem/WeatherApi">Repository Link</a>
 */
@RestController
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(final WeatherService weatherService){
        this.weatherService = weatherService;
    }

    /**
     * Returns the weather for a city from the SMHI api
     *<p>
     * Example request: GET /weather/smhi/stockholm
     * @param city the city to get the weather for from the SMHI api
     * @return the weather for the city from the SMHI api
     * @throws com.example.weatherapi.exceptions.exceptions.ApiConnectionException if the connection to the SMHI api fails
     * @throws com.example.weatherapi.exceptions.exceptions.CityNotFoundException if the city is not found in the database
     */
    @GetMapping(path = "/weather/smhi/{city}")
    public Weather getWeatherBySmhiCity(@PathVariable final String city){
        return weatherService.getWeatherBySmhiCity(city);
    }

    /**
     * Returns the weather for a city from the YR api
     * <p>
     * Example request: GET /weather/yr/stockholm
     * @param city the city to get the weather for from the YR api
     * @return the weather for the city from the YR api
     * @throws com.example.weatherapi.exceptions.exceptions.ApiConnectionException if the connection to the YR api fails
     * @throws com.example.weatherapi.exceptions.exceptions.CityNotFoundException if the city is not found in the database
     */
    @GetMapping(path = "/weather/yr/{city}")
    public Weather getWeatherByYrCity(@PathVariable final String city){
        return weatherService.getWeatherByYrCity(city);
    }

    /**
     * Returns the weather for a city from both the SMHI and YR api merged together
     * <p>
     * Example request: GET /weather/merged/stockholm
     * @param city the city to get the weather for from the SMHI and YR api
     * @return the weather for the city from both the SMHI and YR api merged together
     * @throws com.example.weatherapi.exceptions.exceptions.ApiConnectionException if the connection to either the SMHI or YR api fails
     * @throws com.example.weatherapi.exceptions.exceptions.CityNotFoundException if the city is not found in the database
     */
    @GetMapping(path = "/weather/merged/{city}")
    public Weather getWeatherMerged(@PathVariable final String city){
        return weatherService.getWeatherMerged(city);
    }

}
