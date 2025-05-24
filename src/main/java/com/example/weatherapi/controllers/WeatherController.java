package com.example.weatherapi.controllers;


import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.exceptions.ApiConnectionException;
import com.example.weatherapi.exceptions.CityNotFoundException;
import com.example.weatherapi.services.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller that handles the requests to the api, redirects the requests to the service layer
 * <p>
 * @author Erik Wallenius
 * @see <a href="https://github.com/knottem/WeatherApi">Repository Link</a>
 */
@RestController
@RequestMapping(path = "/weather")
@Tag(name = "Weather", description = "Weather endpoint")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(final WeatherService weatherService){
        this.weatherService = weatherService;
    }

    /**
     * Returns the weather for a city using data from one or more selected APIs (FMI, SMHI, YR).
     * <p>
     * If no API list is provided, the response includes merged data from all available APIs.
     * <p>
     * Example requests:
     * <ul>
     *     <li>GET /v1/weather/stockholm — uses all available APIs (FMI, SMHI, YR)</li>
     *     <li>GET /v1/weather/stockholm?apis=fmi,smhi — uses only the specified APIs</li>
     * </ul>
     *
     * @param city the name of the city to retrieve weather data for
     * @param apis optional list of APIs to query (e.g., fmi, smhi, yr); if omitted, all will be used
     * @return the merged weather data for the specified city
     * @throws ApiConnectionException if all selected APIs are unavailable
     * @throws CityNotFoundException if the city is not found in the database
     */
    @Operation(
            summary = "Get weather by city",
            description = "Returns weather data from all APIs (FMI, SMHI, YR) if no query parameter is specified. " +
                    "If `apis` is provided, only those APIs will be used."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved weather data", content = @Content(schema = @Schema(implementation = Weather.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "City not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping(path = "/{city}")
    public ResponseEntity<Weather> getWeather(
            @PathVariable final String city,
            @Parameter(description = "List of APIs to query (e.g., fmi, smhi, yr). If not provided, all APIs are used.")
            @RequestParam(required = false) String[] apis){
        if(apis == null || apis.length == 0){
            return weatherService.fetchWeatherMergedResponse(city);
        }
        return weatherService.fetchWeatherMergedCustomApisResponse(city, List.of(apis));
    }

}
