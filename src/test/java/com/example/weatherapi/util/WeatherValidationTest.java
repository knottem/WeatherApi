package com.example.weatherapi.util;
;
import com.example.weatherapi.domain.entities.ApiStatus;
import com.example.weatherapi.exceptions.ApiDisabledException;
import com.example.weatherapi.exceptions.InvalidApiUsageException;
import com.example.weatherapi.repositories.ApiStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("unit")
class WeatherValidationTest {

    private Clock fixedClock;
    private ApiStatusRepository apiStatusRepository;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2024-04-24T10:00:00Z"), ZoneOffset.UTC);
        apiStatusRepository = mock(ApiStatusRepository.class);
        WeatherValidation.invalidCache();
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

    @Test
    void shouldThrowExceptionForInvalidApis() {
        when(apiStatusRepository.findAll()).thenReturn(Arrays.asList(
                new ApiStatus(UUID.randomUUID(), "SMHI", true, null),
                new ApiStatus(UUID.randomUUID(), "YR", true, null),
                new ApiStatus(UUID.randomUUID(), "FMI", true, null)
        ));

        List<String> enabledApis = List.of("INVALID_API");

        InvalidApiUsageException exception = assertThrows(InvalidApiUsageException.class, () ->
                WeatherValidation.validateApis(enabledApis, apiStatusRepository));

        assertEquals("Invalid API(s) detected (1): INVALID_API", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForInvalidApisMultipleTimes() {
        when(apiStatusRepository.findAll()).thenReturn(Arrays.asList(
                new ApiStatus(UUID.randomUUID(), "SMHI", true, null),
                new ApiStatus(UUID.randomUUID(), "YR", true, null),
                new ApiStatus(UUID.randomUUID(), "FMI", true, null)
        ));

        List<String> enabledApis = List.of("INVALID_API", "INVALID_API2", "INVALID_API3");

        InvalidApiUsageException exception = assertThrows(InvalidApiUsageException.class, () ->
                WeatherValidation.validateApis(enabledApis, apiStatusRepository));

        assertEquals("Invalid API(s) detected (3): INVALID_API, INVALID_API2, INVALID_API3", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForInactiveApis() {
        when(apiStatusRepository.findAll()).thenReturn(Arrays.asList(
                new ApiStatus(UUID.randomUUID(), "SMHI", true, null),
                new ApiStatus(UUID.randomUUID(), "YR", false, null),
                new ApiStatus(UUID.randomUUID(), "FMI", true, null)
        ));

        List<String> enabledApis = List.of("YR");

        ApiDisabledException exception = assertThrows(ApiDisabledException.class, () ->
                WeatherValidation.validateApis(enabledApis, apiStatusRepository));

        assertEquals("API(s) currently turned off: YR", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForUsingOnlyFmi() {
        when(apiStatusRepository.findAll()).thenReturn(Arrays.asList(
                new ApiStatus(UUID.randomUUID(), "SMHI", true, null),
                new ApiStatus(UUID.randomUUID(), "YR", true, null),
                new ApiStatus(UUID.randomUUID(), "FMI", true, null)
        ));

        List<String> enabledApis = List.of("FMI");

        InvalidApiUsageException exception = assertThrows(InvalidApiUsageException.class, () ->
                WeatherValidation.validateApis(enabledApis, apiStatusRepository));

        assertEquals("FMI API cannot be used alone", exception.getMessage());
    }

    @Test
    void shouldPassForValidApis() {
        when(apiStatusRepository.findAll()).thenReturn(Arrays.asList(
                new ApiStatus(UUID.randomUUID(), "SMHI", true, null),
                new ApiStatus(UUID.randomUUID(), "YR", true, null),
                new ApiStatus(UUID.randomUUID(), "FMI", true, null)
        ));

        List<String> enabledApis = Arrays.asList("SMHI", "YR");

        assertDoesNotThrow(() -> WeatherValidation.validateApis(enabledApis, apiStatusRepository));
    }
}
