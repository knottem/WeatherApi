package com.example.weatherapi.domain.city;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

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
