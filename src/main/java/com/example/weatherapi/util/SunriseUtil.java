package com.example.weatherapi.util;

import com.example.weatherapi.domain.city.City;
import com.example.weatherapi.domain.weather.Weather;
import org.shredzone.commons.suncalc.SunTimes;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Utility methods for calculating sunrise and sunset times for a city.
 * <p>
 * Sunrise and sunset times are calculated in UTC for the current day
 * and the following nine days.
 *
 * @author Erik Wallenius
 * @see <a href="https://github.com/knottem/WeatherApi">Repository Link</a>
 */
public class SunriseUtil {

    private SunriseUtil(){
        throw new IllegalStateException("Utility class");
    }

    /**
     * Calculates sunrise and sunset times for the city associated with the
     * supplied weather data and stores the results in the city object.
     *
     * @param weather the weather object containing the city for which sunrise
     *                and sunset times should be calculated
     */
    public static void getSunriseSunset(Weather weather){
        List<List<ZonedDateTime>> sunRiseSetsList = calculateSunriseSunset(weather.getCity());
        weather.getCity().setSunriseList(sunRiseSetsList.get(0));
        weather.getCity().setSunsetList(sunRiseSetsList.get(1));
    }

    /**
     * Calculates sunrise and sunset times in UTC for the specified city
     * for the current day and the following nine days.
     *
     * @param city the city whose coordinates are used for the calculation
     * @return a list containing the sunrise times as the first element and
     *         the sunset times as the second element
     */
    private static List<List<ZonedDateTime>> calculateSunriseSunset(City city) {
        List<ZonedDateTime> sunRises = new ArrayList<>();
        List<ZonedDateTime> sunSets = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            SunTimes times = SunTimes.compute()
                    .at(city.getLat(), city.getLon())
                    .today()
                    .plusDays(i)
                    .timezone(ZoneOffset.UTC)
                    .execute();

            sunRises.add(Objects.requireNonNull(times.getRise()));
            sunSets.add(Objects.requireNonNull(times.getSet()));
        }
        return List.of(sunRises, sunSets);
    }
}
