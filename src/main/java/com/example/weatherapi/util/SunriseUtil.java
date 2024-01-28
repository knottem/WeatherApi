package com.example.weatherapi.util;

import com.example.weatherapi.domain.City;
import org.shredzone.commons.suncalc.SunTimes;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

public class SunriseUtil {

    private SunriseUtil(){
        throw new IllegalStateException("Utility class");
    }

    public static List<ZonedDateTime> getSunriseSunset(City city) {
        SunTimes times = SunTimes.compute()
                .at(city.getLat(), city.getLon())
                .today()
                .timezone(ZoneOffset.UTC)
                .execute();
        return List.of(Objects.requireNonNull(times.getRise()), Objects.requireNonNull(times.getSet()));
    }
}
