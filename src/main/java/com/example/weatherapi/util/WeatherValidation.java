package com.example.weatherapi.util;

import java.time.Clock;
import java.time.ZonedDateTime;

public class WeatherValidation {

    private WeatherValidation(){
        throw new IllegalStateException("Utility class");
    }

    public static boolean isWeatherValid(ZonedDateTime timeStamp, int minutes, Clock clock) {
        return ZonedDateTime.now(clock).minusMinutes(minutes).isBefore(timeStamp);
    }

    public static boolean isWeatherValid(ZonedDateTime timeStamp, int minutes) {
        return isWeatherValid(timeStamp, minutes, Clock.systemUTC());
    }
}
