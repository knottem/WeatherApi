package com.example.weatherapi.domain.weather;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.*;

@Data
@Builder
public class Weather {

    private String message;

    @Builder.Default
    private Map<LocalDateTime, Float> temperatures = new LinkedHashMap<>();

    public void addTemperature(LocalDateTime validTime, float temperature) {
        temperatures.put(validTime, temperature);
    }

}
