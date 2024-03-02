package com.example.weatherapi.util;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.Weather;
import org.shredzone.commons.suncalc.SunTimes;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SunriseUtil {

    private SunriseUtil(){
        throw new IllegalStateException("Utility class");
    }

    public static void getSunriseSunset(Weather weather){
        List<List<ZonedDateTime>> sunRiseSetsList = calculateSunriseSunset(weather.getCity());
        weather.getCity().setSunriseList(sunRiseSetsList.get(0));
        weather.getCity().setSunsetList(sunRiseSetsList.get(1));
    }

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
