package com.example.weatherapi.domain;

import com.example.weatherapi.domain.entities.WeatherEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
class WeatherEntityTest {

    private WeatherEntity weather;
    int allowedMinutes = 60;

    @BeforeEach
    void setUp() {
        weather = WeatherEntity.builder()
                .timeStamp(ZonedDateTime.ofInstant(Instant.parse("2024-04-24T09:00:05Z"), ZoneOffset.UTC))
                .build();
    }

    @Test
    void weatherShouldBeValidIfWithinLast60Minutes() {
        weather.setClock(Clock.fixed(Instant.parse("2024-04-24T10:00:00Z"), ZoneOffset.UTC));
        assertTrue(weather.isValid(allowedMinutes));
    }

    @Test
    void weatherShouldBeInvalidIfNotWithinLast60Minutes() {
        weather.setClock(Clock.fixed(Instant.parse("2024-04-24T10:01:00Z"), ZoneOffset.UTC));
        assertFalse(weather.isValid(allowedMinutes));
    }
}
