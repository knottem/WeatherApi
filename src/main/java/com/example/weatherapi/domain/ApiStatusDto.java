package com.example.weatherapi.domain;

import lombok.Data;

@Data
public class ApiStatusDto {

    private String api;
    private boolean enabled;

    public ApiStatusDto(String apiName, boolean enabled) {
        this.api = apiName;
        this.enabled = enabled;
    }
}
