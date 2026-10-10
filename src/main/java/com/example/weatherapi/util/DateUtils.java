package com.example.weatherapi.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility methods for working with dates and timestamps.
 *
 * @author Erik Wallenius
 * @see <a href="https://github.com/knottem/WeatherApi">Repository Link</a>
 */
public class DateUtils {

    private DateUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Generates a formatted timestamp a specified number of days after the
     * provided date and time.
     * <p>
     * The resulting timestamp is rounded down to the beginning of the hour
     * by setting minutes, seconds, and nanoseconds to zero.
     *
     * @param zonedDateTime the date and time to use as the starting point
     * @param daysAhead the number of days to add
     * @return the future timestamp formatted as {@code yyyy-MM-dd'T'HH:mm:ss'Z'}
     */
    public static String generateFutureTimestamp(ZonedDateTime zonedDateTime, int daysAhead) {
        ZonedDateTime futureDate = zonedDateTime
                .plusDays(daysAhead)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        return futureDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
    }
}
