package com.example.weatherapi.domain.weather;

import java.time.LocalDateTime;

public class WeatherCache {

    private final WeatherSmhi weatherSmhi;
    private final WeatherYr weatherYr;
    private final LocalDateTime timestamp;

    public WeatherCache(WeatherSmhi weatherSmhi, WeatherYr weatherYr){
        this.weatherSmhi = weatherSmhi;
        this.weatherYr = weatherYr;
        this.timestamp = LocalDateTime.now();
    }

    public WeatherCache(WeatherSmhi weatherSmhi) {
        this.weatherSmhi = weatherSmhi;
        this.weatherYr = null;
        this.timestamp = LocalDateTime.now();
    }

    public WeatherCache(WeatherYr weatherYr) {
        this.weatherSmhi = null;
        this.weatherYr = weatherYr;
        this.timestamp = LocalDateTime.now();
    }

    public boolean isValid(int hours) {
        return LocalDateTime.now().minusHours(hours).isBefore(timestamp);
    }

    public WeatherSmhi getWeather() {
        return weatherSmhi;
    }

    public WeatherYr getWeatherYr() {
        return weatherYr;
    }
}
