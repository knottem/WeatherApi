package com.example.weatherapi.domain.weather;

import java.time.LocalDateTime;

public class WeatherCache {

    private final WeatherSmhi weatherSmhi;
    private final WeatherYr weatherYr;
    private final LocalDateTime timestamp;

    private final Weather weather;

    public WeatherCache(WeatherSmhi weatherSmhi, WeatherYr weatherYr, Weather weather){
        this.weatherSmhi = weatherSmhi;
        this.weatherYr = weatherYr;
        this.weather = weather;
        this.timestamp = LocalDateTime.now();
    }

    public WeatherCache(WeatherSmhi weatherSmhi) {
        this.weatherSmhi = weatherSmhi;
        this.weatherYr = null;
        this.weather = null;
        this.timestamp = LocalDateTime.now();
    }

    public WeatherCache(WeatherYr weatherYr) {
        this.weatherSmhi = null;
        this.weather = null;
        this.weatherYr = weatherYr;
        this.timestamp = LocalDateTime.now();
    }

    public WeatherCache(Weather weather) {
        this.weatherSmhi = null;
        this.weatherYr = null;
        this.weather = weather;
        this.timestamp = LocalDateTime.now();
    }

    public boolean isValid(int hours) {
        return LocalDateTime.now().minusHours(hours).isBefore(timestamp);
    }

    public WeatherSmhi getWeatherSmhi() {
        return weatherSmhi;
    }

    public WeatherYr getWeatherYr() {
        return weatherYr;
    }

    public Weather getWeather() {
        return weather;
    }
}
