package com.example.weatherapi.util;

import com.example.weatherapi.domain.weather.WeatherYr;

import java.util.HashMap;
import java.util.Map;

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

    private static Integer getWeatherCodeYRMap(String weatherCode) {
        if (weatherCode == null) {
            return -1;
        }
        return weatherCodeYrMap.getOrDefault(weatherCode.replaceAll("_(day|night)$", ""), -1);
    }

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
