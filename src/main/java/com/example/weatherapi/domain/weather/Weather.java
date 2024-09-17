package com.example.weatherapi.domain.weather;

import com.example.weatherapi.domain.City;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//This is the final representation of the data that is returned to the user after the data has been fetched from the APIs and merged
public class Weather {

    private String message;

    private ZonedDateTime timestamp;

    private City city;

    @Builder.Default
    private Information information = new Information();

    @Builder.Default
    private Map<ZonedDateTime, WeatherData> weatherData = new LinkedHashMap<>();

    public void addWeatherData(ZonedDateTime validTime,
                               float temperature,
                               int weatherCode,
                               float windSpeed,
                               float windDirection,
                               float precipitation) {
        weatherData.put(validTime, WeatherData.builder()
                .temperature(temperature)
                .weatherCode(weatherCode)
                .windSpeed(windSpeed)
                .windDirection(windDirection)
                .precipitation(precipitation)
                .build());
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Information {
        private final String Temperature = "Celsius";
        private final String WindSpeed = "m/s";
        private final String Precipitation = "mm/hr - kg/m2/h";
        private final String Time = "UTC";
        private final String Humidity = "%";

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeatherData {
        private float temperature;
        private int weatherCode;
        private float windSpeed;
        private float windDirection;
        private float precipitation;
        private float humidity;
    }

}
