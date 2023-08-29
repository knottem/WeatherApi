package com.example.frontend.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
public class Weather {

    private String message;

    @Builder.Default
    private Information information = new Information();

    @Builder.Default
    private Map<LocalDateTime, WeatherData> weatherData = new LinkedHashMap<>();


    //Might not be needed, just copy pasted the backend class.
    public void addWeatherData(LocalDateTime validTime,
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
    private static class Information {
        private final String Temperature = "Celsius";
        private final String windSpeed = "m/s";
        private final String Precipitation = "mm/hr - kg/m2/h";
    }

    @Data
    @Builder
    public static class WeatherData {
        private float temperature;
        private int weatherCode;
        private float windSpeed;
        private float windDirection;
        private float precipitation;
    }
}
