package com.example.weatherapi.domain.weather.weatherFmi;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class MeasurementTVP {

    @JacksonXmlProperty(localName = "time")
    private String time;

    @JacksonXmlProperty(localName = "value")
    private Double value;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
