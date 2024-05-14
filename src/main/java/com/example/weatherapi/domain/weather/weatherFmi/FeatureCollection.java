package com.example.weatherapi.domain.weather.weatherFmi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeatureCollection {

    @JacksonXmlProperty(localName = "member")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<FeatureMember> members;

    public List<FeatureMember> getMembers() {
        return members;
    }

    public void setMembers(List<FeatureMember> members) {
        this.members = members;
    }
}