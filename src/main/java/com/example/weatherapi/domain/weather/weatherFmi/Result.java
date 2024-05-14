package com.example.weatherapi.domain.weather.weatherFmi;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Result {

    @JacksonXmlProperty(localName = "MeasurementTimeseries")
    private MeasurementTimeseries measurementTimeseries;

    public MeasurementTimeseries getMeasurementTimeseries() {
        return measurementTimeseries;
    }

    public void setMeasurementTimeseries(MeasurementTimeseries measurementTimeseries) {
        this.measurementTimeseries = measurementTimeseries;
    }
}
