package com.example.weatherapi.domain.weather;

import java.time.LocalDateTime;
import java.util.List;

public record WeatherSmhi(LocalDateTime approvedTime, LocalDateTime referenceTime, Geometry geometry, List<TimeSerie> timeSeries) {
    public record TimeSerie(LocalDateTime validTime, List<Parameter> parameters) { }
    public record Parameter(String name, String levelType, int level, String unit, List<Float> values) { }
    public record Geometry(String type, List<List<Float>> coordinates) { }

}
