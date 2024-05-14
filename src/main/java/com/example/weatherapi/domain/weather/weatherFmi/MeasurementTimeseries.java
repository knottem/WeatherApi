package com.example.weatherapi.domain.weather.weatherFmi;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import java.util.List;


public class MeasurementTimeseries {

    @JacksonXmlProperty(isAttribute = true, localName = "id")
    private String id;

    @JacksonXmlProperty(localName = "point")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Point> points;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public static class Point {
        @JacksonXmlProperty(localName = "MeasurementTVP")
        private MeasurementTVP measurementTVP;

        public MeasurementTVP getMeasurementTVP() {
            return measurementTVP;
        }

        public void setMeasurementTVP(MeasurementTVP measurementTVP) {
            this.measurementTVP = measurementTVP;
        }
    }
}
