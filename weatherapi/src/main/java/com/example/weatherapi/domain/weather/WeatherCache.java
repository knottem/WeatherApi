package com.example.weatherapi.domain.weather;

import java.time.LocalDateTime;

public class WeatherCache {

    private final WeatherSmhi weatherSmhi;
    private final LocalDateTime timestamp;

    public WeatherCache(WeatherSmhi weatherSmhi) {
        this.weatherSmhi = weatherSmhi;
        this.timestamp = LocalDateTime.now();
    }

    public boolean isValid(int hours) {
        return LocalDateTime.now().minusHours(hours).isBefore(timestamp);
    }

    public WeatherSmhi getWeather() {
        return weatherSmhi;
    }
}
