package com.example.weatherapi.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch(
                        "/error",
                        "/weather/**",
                        "/status/**",
                        "/city/names",
                        "/city/search"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi authenticatedApi() {
        return GroupedOpenApi.builder()
                .group("authenticated")
                .pathsToMatch("/**")
                .build();
    }
}
