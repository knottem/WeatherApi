package com.example.weatherapi.domain.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "FeatureCollection")
public class WeatherFmi {

    @JacksonXmlProperty(localName = "member")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<FeatureMember> members;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FeatureMember {

        @JacksonXmlProperty(localName = "PointTimeSeriesObservation")
        private PointTimeSeriesObservation pointTimeSeriesObservation;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PointTimeSeriesObservation {

        @JacksonXmlProperty(localName = "result")
        private Result result;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {

        @JacksonXmlProperty(localName = "MeasurementTimeseries")
        private MeasurementTimeseries measurementTimeseries;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MeasurementTimeseries {

        @JacksonXmlProperty(isAttribute = true)
        private String id;

        @JacksonXmlProperty(localName = "point")
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<Point> points;

        @Getter
        @Setter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Point {

            @JacksonXmlProperty(localName = "MeasurementTVP")
            private MeasurementTVP measurementTVP;
        }
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MeasurementTVP {

        @JacksonXmlProperty(localName = "time")
        private String time;

        @JacksonXmlProperty(localName = "value")
        private Double value;
    }

}