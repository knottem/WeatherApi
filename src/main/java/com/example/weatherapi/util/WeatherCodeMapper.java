package com.example.weatherapi.util;

import com.example.weatherapi.domain.weather.WeatherYr;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for converting YR weather symbol codes into the integer
 * weather codes used by the application.
 * <p>
 * YR represents weather conditions using string-based symbol codes, while
 * the application uses numeric weather codes compatible with the SMHI
 * weather code format. Day and night suffixes in YR symbol codes are
 * ignored during conversion.
 *
 * @author Erik Wallenius
 * @see <a href="https://github.com/knottem/WeatherApi">Repository Link</a>
 */
public class WeatherCodeMapper {

    private WeatherCodeMapper() {
        throw new IllegalStateException("Utility class");
    }
    private static final Map<String, Integer> weatherCodeYrMap = new HashMap<>();

    static {
        weatherCodeYrMap.put("clearsky",1);
        weatherCodeYrMap.put("cloudy",5);
        weatherCodeYrMap.put("fair",4);
        weatherCodeYrMap.put("fog",7);
        weatherCodeYrMap.put("heavyrain",20);
        weatherCodeYrMap.put("heavyrainandthunder",21);
        weatherCodeYrMap.put("heavyrainshowers",10);
        weatherCodeYrMap.put("heavyrainshowersandthunder",11);
        weatherCodeYrMap.put("heavysleet",24);
        weatherCodeYrMap.put("heavysleetandthunder",21);
        weatherCodeYrMap.put("heavysleetshowers",14);
        weatherCodeYrMap.put("heavysleetshowersandthunder",14);
        weatherCodeYrMap.put("heavysnow",27);
        weatherCodeYrMap.put("heavysnowandthunder",27);
        weatherCodeYrMap.put("heavysnowshowers",17);
        weatherCodeYrMap.put("heavysnowshowersandthunder",17);
        weatherCodeYrMap.put("lightrain",18);
        weatherCodeYrMap.put("lightrainandthunder",21);
        weatherCodeYrMap.put("lightrainshowers",8);
        weatherCodeYrMap.put("lightrainshowersandthunder",11);
        weatherCodeYrMap.put("lightsleet",22);
        weatherCodeYrMap.put("lightsleetandthunder",21);
        weatherCodeYrMap.put("lightsleetshowers",12);
        weatherCodeYrMap.put("lightsleetshowersandthunder",11);
        weatherCodeYrMap.put("lightsnow",25);
        weatherCodeYrMap.put("lightsnowandthunder",21);
        weatherCodeYrMap.put("lightsnowshowers",15);
        weatherCodeYrMap.put("lightsnowshowersandthunder",11);
        weatherCodeYrMap.put("partlycloudy",3);
        weatherCodeYrMap.put("rain",19);
        weatherCodeYrMap.put("rainandthunder",21);
        weatherCodeYrMap.put("rainshowers",9);
        weatherCodeYrMap.put("rainshowersandthunder",11);
        weatherCodeYrMap.put("sleet",23);
        weatherCodeYrMap.put("sleetandthunder",21);
        weatherCodeYrMap.put("sleetshowers",13);
        weatherCodeYrMap.put("sleetshowersandthunder",11);
        weatherCodeYrMap.put("snow",26);
        weatherCodeYrMap.put("snowandthunder",21);
        weatherCodeYrMap.put("snowshowers",16);
        weatherCodeYrMap.put("snowshowersandthunder",11);
    }

    /**
     * Converts a YR weather symbol code into the corresponding numeric
     * weather code used by the application.
     * <p>
     * Any trailing {@code _day} or {@code _night} suffix is removed before
     * looking up the weather code.
     *
     * @param weatherCode the YR weather symbol code to convert
     * @return the corresponding numeric weather code, or {@code -1} if the
     *         code is {@code null} or cannot be mapped
     */
    private static Integer getWeatherCodeYRMap(String weatherCode) {
        if (weatherCode == null) {
            return -1;
        }
        return weatherCodeYrMap.getOrDefault(weatherCode.replaceAll("_(day|night)$", ""), -1);
    }

    /**
     * Extracts the most immediate available YR weather symbol from the
     * supplied time series and converts it into the numeric weather code
     * used by the application.
     * <p>
     * The method checks the one-hour forecast first, followed by the
     * six-hour and twelve-hour forecasts.
     *
     * @param timeSeries the YR time series containing forecast data
     * @return the corresponding numeric weather code, or {@code -1} if no
     *         supported forecast period or weather symbol is available
     */
    public static int mapToWeatherCodeYR(WeatherYr.TimeSeries timeSeries) {
        if(timeSeries.data().next_1_hours() != null){
            return getWeatherCodeYRMap(timeSeries.data().next_1_hours().summary().symbol_code());
        } else if(timeSeries.data().next_6_hours() != null){
            return getWeatherCodeYRMap(timeSeries.data().next_6_hours().summary().symbol_code());
        } else if(timeSeries.data().next_12_hours() != null){
            return getWeatherCodeYRMap(timeSeries.data().next_12_hours().summary().symbol_code());
        } else {
            return -1;
        }
    }
}
