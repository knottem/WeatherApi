package com.example.weatherapi.domain.weather;

import com.example.weatherapi.domain.city.City;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//This is the final representation of the data that is returned to the user after the data has been fetched from the APIs and merged
public class Weather {

    @Schema(description = "Summary of how the response was produced", example = "Merged weather for Stockholm from FMI, SMHI and YR")
    private String message;

    @Schema(description = "When this response was generated", example = "2026-02-28T12:12:06.1843843Z")
    private ZonedDateTime timestamp;

    private City city;

    @Builder.Default
    private Information information = new Information();

    @Schema(
            description = "Weather datapoints keyed by ISO-8601 UTC timestamp",
            type = "object",
            additionalProperties = Schema.AdditionalPropertiesValue.TRUE,
            example = """
  {
    "2026-02-28T11:00:00Z": {
      "temperature": 1.9,
      "weatherCode": 5,
      "windSpeed": 1.4,
      "windDirection": 48,
      "precipitation": 0.0,
      "humidity": 91.5
    }
  }
  """
    )
    @Builder.Default
    private Map<ZonedDateTime, WeatherData> weatherData = new LinkedHashMap<>();

    @Getter
    @Schema(name = "Information", description = "Units and time base used in the response")
    public static class Information {

        @Schema(description = "Unit of temperature", example = "Celsius")
        private final String temperature = "Celsius";

        @Schema(description = "Unit of wind speed", example = "m/s")
        private final String windSpeed = "m/s";

        @Schema(description = "Unit of precipitation", example = "mm/h")
        private final String precipitation = "mm/h";

        @Schema(description = "Time zone / time base", example = "UTC")
        private final String time = "UTC";

        @Schema(description = "Unit of humidity", example = "%")
        private final String humidity = "%";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "WeatherData", description = "Weather values for a specific timestamp")
    public static class WeatherData {

        @Schema(example = "1.9", description = "Air temperature in Celsius")
        private float temperature;

        @Schema(example = "5", description = "Weather condition code")
        private int weatherCode;

        @Schema(example = "1.4", description = "Wind speed in m/s")
        private float windSpeed;

        @Schema(example = "48", description = "Wind direction in degrees", minimum = "0", maximum = "360")
        private float windDirection;

        @Schema(example = "0.0", description = "Precipitation in mm/hr")
        private float precipitation;

        @Schema(example = "50", description = "Relative humidity in percent", minimum = "0", maximum = "100")
        private float humidity;
    }

    public void addWeatherData(ZonedDateTime validTime,
                               float temperature,
                               int weatherCode,
                               float windSpeed,
                               float windDirection,
                               float humidity,
                               float precipitation) {
        weatherData.put(validTime, WeatherData.builder()
                .temperature(temperature)
                .weatherCode(weatherCode)
                .windSpeed(windSpeed)
                .windDirection(windDirection)
                .humidity(humidity)
                .precipitation(precipitation)
                .build());
    }

}
