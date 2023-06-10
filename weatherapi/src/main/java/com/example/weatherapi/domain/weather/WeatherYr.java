package com.example.weatherapi.domain.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

//Added this how the api from yr will look like
public record WeatherYr(Geometry geometry, Properties properties, String type) {
    public record Geometry(String type, List<Double> coordinates) {}
    public record Properties(Meta meta, List<TimeSeries> timeseries) {}
    public record Meta(String updated_at, Units units) {}
    public record Units(String air_pressure_at_sea_level, String air_temperature, String cloud_area_fraction,
                        String precipitation_amount, String relative_humidity, String wind_from_direction,
                        String wind_speed) {}
    public record TimeSeries(String time, Data data) {}
    public record Data(Instant instant, NextHours next_1_hours, NextHours next_6_hours, NextHours next_12_hours) {}
    public record Instant(Details details) {}
    public record NextHours(Summary summary, Details details) {}
    public record Details(double air_pressure_at_sea_level, double air_temperature, double cloud_area_fraction,
                          double precipitation_amount, double relative_humidity, double wind_from_direction,
                          double wind_speed) {}
    public record Summary(String symbol_code) {}

}
