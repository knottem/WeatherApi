package com.example.weatherapi.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityDto {
    private String name; // Default name
    private String en; // Default name_en
}

