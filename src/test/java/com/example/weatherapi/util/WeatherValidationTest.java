package com.example.weatherapi.util;

import com.example.weatherapi.util.WeatherValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static com.example.weatherapi.util.WeatherValidation.isWeatherValid;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
class WeatherValidationTest {

    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2024-04-24T10:00:00Z"), ZoneOffset.UTC);
    }

    @Test
    void weatherShouldBeValidIfWithinLast60Minutes() {
        ZonedDateTime timeStamp = ZonedDateTime.ofInstant(Instant.parse("2024-04-24T09:00:05Z"), ZoneOffset.UTC);
        assertTrue(WeatherValidation.isWeatherValid(timeStamp, 60, fixedClock));
    }

    @Test
    void weatherShouldBeInvalidIfNotWithinLast60Minutes() {
        fixedClock = Clock.fixed(Instant.parse("2024-04-24T10:01:00Z"), ZoneOffset.UTC);
        ZonedDateTime timeStamp = ZonedDateTime.ofInstant(Instant.parse("2024-04-24T09:00:05Z"), ZoneOffset.UTC);
        assertFalse(WeatherValidation.isWeatherValid(timeStamp, 60, fixedClock));
    }
}
