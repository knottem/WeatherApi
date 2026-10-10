package com.example.weatherapi.domain.city;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * Represents a request to search for a city based on its geographical coordinates.
 * This record is used to encapsulate the latitude and longitude parameters required for the search.
 * <p>
 * The latitude must be between 55 and 71 degrees, and the longitude must be between 4 and 32 degrees.
 * </p>
 * @author Erik Wallenius
 * @see <a href="https://github.com/knottem/WeatherApi">Repository Link</a>
 */
@Builder
public record CitySearchRequest(

        @Schema(description = "Latitute of the location", example = "59.3293")
        @NotNull(message = "Latitude cannot be null")
        @Min(value = 55, message = "Latitude must be between 55 and 71")
        @Max(value = 71, message = "Latitude must be between 55 and 71")
        Double lat,

        @Schema(description = "Longitude of the location", example = "18.0686")
        @NotNull(message = "Longitude cannot be null")
        @Min(value = 4, message = "Longitude must be between 4 and 32")
        @Max(value = 32, message = "Longitude must be between 4 and 32")
        Double lon
) { }
