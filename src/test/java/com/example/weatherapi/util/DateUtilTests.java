package com.example.weatherapi.util;

import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static com.example.weatherapi.util.DateUtils.generateFutureTimestamp;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DateUtilTests {

    @Test
    void testGenerateFutureTimestamp() {
        int daysAhead = 10;
        String actualTimestamp = generateFutureTimestamp( ZonedDateTime.of(2024, 5, 4, 10, 15, 30, 0, ZoneOffset.UTC), daysAhead);
        String expectedTimestamp = "2024-05-14T10:00:00Z";
        assertEquals(expectedTimestamp, actualTimestamp);
    }
}
