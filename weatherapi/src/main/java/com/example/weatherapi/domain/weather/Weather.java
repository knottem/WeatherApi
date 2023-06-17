package com.example.weatherapi.domain.weather;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.*;

@Data
@Builder
public class Weather {

    private String message;

    @Builder.Default
    private Map<LocalDateTime, WeatherData> weatherData = new LinkedHashMap<>();

    public void addWeatherData(LocalDateTime validTime, float temperature, int weatherCode, float windSpeed, float windDirection) {
        weatherData.put(validTime, WeatherData.builder()
                .temperature(temperature)
                .weatherCode(weatherCode)
                .windSpeed(windSpeed)
                .windDirection(windDirection)
                .build());
    }

    @Data
    @Builder
    public static class WeatherData {
        private float temperature;
        private int weatherCode;
        private float windSpeed;
        private float windDirection;
    }

}
