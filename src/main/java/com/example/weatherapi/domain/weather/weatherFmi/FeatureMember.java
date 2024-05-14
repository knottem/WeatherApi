package com.example.weatherapi.domain.weather.weatherFmi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeatureMember {

    @JacksonXmlProperty(localName = "PointTimeSeriesObservation")
    private PointTimeSeriesObservation pointTimeSeriesObservation;

    public PointTimeSeriesObservation getPointTimeSeriesObservation() {
        return pointTimeSeriesObservation;
    }

    public void setPointTimeSeriesObservation(PointTimeSeriesObservation pointTimeSeriesObservation) {
        this.pointTimeSeriesObservation = pointTimeSeriesObservation;
    }
}

