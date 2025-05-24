package com.example.weatherapi.domain.city;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class City {

    @Schema(example = "Stockholm")
    @NotBlank(message = "City name cannot be null or empty")
    private String name;

    @Schema(example = "59.3294")
    @NotNull(message = "Latitude cannot be null")
    @Min(value = 55, message = "Latitude must be between 55 and 71")
    @Max(value = 71, message = "Latitude must be between 55 and 71")
    private Double lat;

    @Schema(example = "18.0686")
    @NotNull(message = "Longitude cannot be null")
    @Min(value = 4, message = "Longitude must be between 4 and 32")
    @Max(value = 32, message = "Longitude must be between 4 and 32")
    private Double lon;


    private List<ZonedDateTime> sunriseList;
    private List<ZonedDateTime> sunsetList;
}
