package com.example.weatherapi.domain;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class City {

    @NotBlank(message = "City name cannot be null or empty")
    private String name;

    @NotNull(message = "Longitude cannot be null")
    @Min(value = 4, message = "Longitude must be between 4 and 32")
    @Max(value = 32, message = "Longitude must be between 4 and 32")
    private Double lon;

    @NotNull(message = "Latitude cannot be null")
    @Min(value = 55, message = "Latitude must be between 55 and 71")
    @Max(value = 71, message = "Latitude must be between 55 and 71")
    private Double lat;
}
