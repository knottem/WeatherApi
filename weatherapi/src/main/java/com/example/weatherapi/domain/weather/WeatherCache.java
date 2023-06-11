package com.example.weatherapi.domain.weather;

import java.time.LocalDateTime;

public class WeatherCache {

    private final LocalDateTime timestamp;

    private final Weather weather;

    public WeatherCache(Weather weather) {
        this.weather = weather;
        this.timestamp = LocalDateTime.now();
    }

    public boolean isValid(int hours) {
        return LocalDateTime.now().minusHours(hours).isBefore(timestamp);
    }

    public Weather getWeather() {
        return weather;
    }
}
