package com.example.weatherapi.controllers;

import com.example.weatherapi.domain.city.City;
import com.example.weatherapi.domain.city.CitySearchRequest;
import com.example.weatherapi.domain.city.CitySearchResponse;
import com.example.weatherapi.domain.dto.CityDto;
import com.example.weatherapi.domain.entities.CityEntity;
import com.example.weatherapi.services.CityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path ="/city")
@Tag(name = "City", description = "City management endpoints")
public class CityController {

    private final CityService cityService;

    public CityController(final CityService cityService) {
        this.cityService = cityService;
    }

    @Operation(summary = "Get a city by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved a city data", content =
                    @Content(schema = @Schema(implementation = CityEntity.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "No City was found", content =
                    @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content =
                    @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping(path = "/{name}")
    public CityEntity retrieveCity(@PathVariable final String name) {
        return cityService.getCityByName(name);
    }

    @Operation(summary = "Create a new city")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created a new city", content =
                    @Content(schema = @Schema(implementation = CityEntity.class))),
            @ApiResponse(responseCode = "400", description = "Invalid city data", content =
                    @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content =
                    @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping(path = "/create")
    public ResponseEntity<CityEntity> addCity(@Valid @RequestBody City city) {
        return new ResponseEntity<>(cityService.addCity(city), HttpStatus.CREATED);
    }

    @Operation(summary = "Get all cities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all cities", content =
                    @Content(array = @ArraySchema(schema = @Schema(implementation = CityEntity.class)))),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content =
                    @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping(path = "/all")
    public List<CityEntity> retrieveAllCities() {
        return cityService.getAllCities();
    }

    @Operation(summary = "Delete a city by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted the city", content =
                    @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "No City was found", content =
                    @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content =
                    @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping(path = "/delete/{name}")
    public ResponseEntity<String> deleteCity(@PathVariable final String name) {
        return new ResponseEntity<>(cityService.deleteCity(name), HttpStatus.OK);
    }

    @Operation(summary = "Get all city names")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all city names", content =
                    @Content(array = @ArraySchema(schema = @Schema(implementation = CityDto.class)))),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content =
                    @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping(path = "/names")
    public List<CityDto> retrieveAllCityNames() {
        return cityService.getAllCityNames();
    }

    @Operation(summary = "Search for a city by latitude and longitude")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved city search results", content =
                    @Content(schema = @Schema(implementation = CitySearchResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid latitude or longitude", content =
                    @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content =
                    @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping(path = "/search")
    public CitySearchResponse searchCity(@Valid @RequestBody CitySearchRequest request){
        return cityService.searchCity(request.lat(), request.lon());
    }

}
