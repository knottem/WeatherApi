package com.example.weatherapi.domain.weather;

import java.time.LocalDateTime;
import java.util.List;

//The API from smhi returns a JSON object with a lot of data. This class is a representation of that JSON object.
//The JSON object is a bit complex, so took some time to figure out how to represent it in Java, but I think this is a good solution.
//For future reference, this is a good tool to use when converting JSON to Java: https://json2csharp.com/json-to-pojo
//converted everything to records to make it more compact
public record WeatherSmhi(LocalDateTime approvedTime, LocalDateTime referenceTime, Geometry geometry, List<TimeSerie> timeSeries) {
    public record TimeSerie(LocalDateTime validTime, List<Parameter> parameters) { }
    public record Parameter(String name, String levelType, int level, String unit, List<Float> values) { }
    public record Geometry(String type, List<List<Float>> coordinates) { }

}