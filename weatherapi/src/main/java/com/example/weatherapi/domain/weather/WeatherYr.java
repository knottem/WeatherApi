package com.example.weatherapi.domain.weather;

import java.time.LocalDateTime;
import java.util.List;

//Added this how the api from yr will look like
public record WeatherYr(Geometry geometry, Properties properties, String type) {
    public record Geometry(String type, List<Double> coordinates) {}
    public record Properties(Meta meta, List<TimeSeries> timeseries) {}
    public record Meta(String updated_at, Units units) {}
    public record Units(String air_pressure_at_sea_level, String air_temperature, String cloud_area_fraction,
                        String precipitation_amount, String relative_humidity, String wind_from_direction,
                        String wind_speed) {}
    public record TimeSeries(LocalDateTime time, Data data) {}
    public record Data(Instant instant, NextHours next_1_hours, NextHours next_6_hours, NextHours next_12_hours) {}
    public record Instant(Details details) {}
    public record NextHours(Summary summary, Details details) {}
    public record Details(float air_pressure_at_sea_level, float air_temperature, float cloud_area_fraction,
                          float precipitation_amount, float relative_humidity, float wind_from_direction,
                          float wind_speed) {}
    public record Summary(String symbol_code) {}

}
