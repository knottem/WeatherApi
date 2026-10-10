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

/**
 * Represents a city with its geographical coordinates and sunrise and sunset times.
 * @author Erik Wallenius
 * @see <a href="https://github.com/knottem/WeatherApi">Repository Link</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class City {

    /**
     * The name of the city.
     */
    @Schema(example = "Stockholm")
    @NotBlank(message = "City name cannot be null or empty")
    private String name;

    /**
     * The latitude of the city.
     * Must be between 55 and 71 degrees.
     */
    @Schema(example = "59.3294")
    @NotNull(message = "Latitude cannot be null")
    @Min(value = 55, message = "Latitude must be between 55 and 71")
    @Max(value = 71, message = "Latitude must be between 55 and 71")
    private Double lat;

    /**
     * The longitude of the city.
     * Must be between 4 and 32 degrees.
     */
    @Schema(example = "18.0686")
    @NotNull(message = "Longitude cannot be null")
    @Min(value = 4, message = "Longitude must be between 4 and 32")
    @Max(value = 32, message = "Longitude must be between 4 and 32")
    private Double lon;

    /**
     * Sunrise times for the next 10 days in UTC, ordered chronologically.
     */
    @Schema(description = "Sunrise times for the next 10 days in UTC, ordered chronologically (ISO-8601)", example = """
    [
      "2026-02-28T06:10:30Z",
      "2026-03-01T06:07:39Z",
      "2026-03-02T06:04:48Z"
    ]
    """
    )
    private List<ZonedDateTime> sunriseList;

    /**
     * Sunset times for the next 10 days in UTC, ordered chronologically.
     */
    @Schema(description = "Sunset times for the next 10 days in UTC, ordered chronologically (ISO-8601)", example = """
    [
      "2026-02-28T16:37:01Z",
      "2026-03-01T16:39:28Z",
      "2026-03-02T16:41:54Z"
    ]
    """
    )
    private List<ZonedDateTime> sunsetList;
}
