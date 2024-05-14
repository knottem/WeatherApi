package com.example.weatherapi.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private DateUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String generateFutureTimestamp(ZonedDateTime zonedDateTime, int daysAhead) {
        ZonedDateTime futureDate = zonedDateTime
                .plusDays(daysAhead)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);;
        return futureDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
    }
}
