package com.example.weatherapi.domain.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherSmhi(
        ZonedDateTime createdTime,
        ZonedDateTime referenceTime,
        Geometry geometry,
        List<TimeSerie> timeSeries
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TimeSerie(
            ZonedDateTime time,
            ZonedDateTime intervalParametersStartTime,
            Data data
    ) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(
            Float air_temperature,
            Float wind_from_direction,
            Float wind_speed,
            Float wind_speed_of_gust,
            Float relative_humidity,
            Float air_pressure_at_mean_sea_level,
            Float visibility_in_air,
            Float thunderstorm_probability,
            Float probability_of_frozen_precipitation,
            Float cloud_area_fraction,
            Float low_type_cloud_area_fraction,
            Float medium_type_cloud_area_fraction,
            Float high_type_cloud_area_fraction,
            Float cloud_base_altitude,
            Float cloud_top_altitude,
            Float precipitation_amount_mean,
            Float precipitation_amount_min,
            Float precipitation_amount_max,
            Float precipitation_amount_median,
            Float precipitation_amount_mean_deterministic,
            Float probability_of_precipitation,
            Float precipitation_frozen_part,
            Float predominant_precipitation_type_at_surface,
            Integer symbol_code
    ) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Geometry(
            String type,
            List<Float> coordinates
    ) { }
}