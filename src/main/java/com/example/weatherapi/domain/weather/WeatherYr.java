package com.example.weatherapi.domain.weather;

import java.time.ZonedDateTime;
import java.util.List;

//Added this how the api from yr will look like
//The JSON object is a bit complex, so took some time to figure out how to represent it in Java, but I think this is a good solution.
//For future reference, this is a good tool to use when converting JSON to Java: https://json2csharp.com/json-to-pojo
//converted everything to records to make it more compact
public record WeatherYr(Geometry geometry, Properties properties, String type) {
    public record Geometry(String type, List<Double> coordinates) {}
    public record Properties(Meta meta, List<TimeSeries> timeseries) {}
    public record Meta(String updated_at, Units units) {}
    public record Units(String air_pressure_at_sea_level, String air_temperature, String cloud_area_fraction,
                        String precipitation_amount, String relative_humidity, String wind_from_direction,
                        String wind_speed) {}
    public record TimeSeries(ZonedDateTime time, Data data) {}
    public record Data(Instant instant, NextHours next_1_hours, NextHours next_6_hours, NextHours next_12_hours) {}
    public record Instant(Details details) {}
    public record NextHours(Summary summary, Details details) {}
    public record Details(Float air_pressure_at_sea_level, Float air_temperature, Float cloud_area_fraction,
                          Float precipitation_amount, Float relative_humidity, Float wind_from_direction,
                          Float wind_speed) {}
    public record Summary(String symbol_code) {}

}
