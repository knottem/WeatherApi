package com.example.weatherapi.util;

import com.example.weatherapi.domain.City;
import org.shredzone.commons.suncalc.SunTimes;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SunriseUtil {

    private SunriseUtil(){
        throw new IllegalStateException("Utility class");
    }

    public static List<List<ZonedDateTime>> getSunriseSunset(City city) {
        List<ZonedDateTime> sunRises = new ArrayList<>();
        List<ZonedDateTime> sunSets = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
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
