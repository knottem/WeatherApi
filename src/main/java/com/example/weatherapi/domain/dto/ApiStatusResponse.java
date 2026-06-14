package com.example.weatherapi.domain.dto;

import com.example.weatherapi.domain.entities.ApiStatus;
import lombok.Builder;

import java.util.List;

@Builder
public record ApiStatusResponse(List<ApiStatusDto> apis) {

    @Builder
    public record ApiStatusDto(String api, boolean enabled) {

        public static List<ApiStatusDto> from(List<ApiStatus> apiStatuses){
            if(apiStatuses == null || apiStatuses.isEmpty()) return List.of();
            return apiStatuses.stream()
                    .map(apiStatus -> ApiStatusDto.builder()
                            .api(apiStatus.getApiName())
                            .enabled(apiStatus.isActive())
                            .build()).toList();
        }
    }
}
