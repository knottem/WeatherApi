package com.example.weatherapi.util;

import com.example.weatherapi.domain.entities.CityEntity;
import com.example.weatherapi.domain.entities.WeatherDataEntity;
import com.example.weatherapi.domain.entities.WeatherEntity;
import com.example.weatherapi.domain.weather.Weather;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

import static com.example.weatherapi.util.CityMapper.toModel;

public class WeatherMapper {

    private WeatherMapper(){
        throw new IllegalStateException("Utility class");
    }

    public static WeatherEntity convertToWeatherEntity(Weather weather, CityEntity cityEntity) {
        return WeatherEntity.builder()
                .message(weather.getMessage())
                .timeStamp(weather.getTimeStamp())
                .city(cityEntity)
                .build();
    }

    public static Weather convertToWeather(WeatherEntity weatherEntity) {
        return Weather.builder()
                .message(weatherEntity.getMessage())
                .timeStamp(weatherEntity.getTimeStamp())
                .city(toModel(weatherEntity.getCity()))
                .weatherData(convertToWeatherDataMap(weatherEntity.getWeatherDataList()))
                .build();
    }

    private static Map<LocalDateTime, Weather.WeatherData> convertToWeatherDataMap(List<WeatherDataEntity> weatherDataEntities) {
        return weatherDataEntities.stream()
                .sorted(Comparator.comparing(WeatherDataEntity::getValidTime))
                .collect(Collectors.toMap(
                        WeatherDataEntity::getValidTime,
                        entity -> Weather.WeatherData.builder()
                                .temperature(entity.getTemperature())
                                .weatherCode(entity.getWeatherCode())
                                .windSpeed(entity.getWindSpeed())
                                .windDirection(entity.getWindDirection())
                                .precipitation(entity.getPrecipitation())
                                .build(),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    public static List<WeatherDataEntity> convertToWeatherDataEntity(Map<LocalDateTime, Weather.WeatherData> weatherData, WeatherEntity weatherEntity) {
        return weatherData.entrySet().stream()
                .map(entry -> WeatherDataEntity.builder()
                        .validTime(entry.getKey())
                        .temperature(entry.getValue().getTemperature())
                        .weatherCode(entry.getValue().getWeatherCode())
                        .windSpeed(entry.getValue().getWindSpeed())
                        .windDirection(entry.getValue().getWindDirection())
                        .precipitation(entry.getValue().getPrecipitation())
                        .weatherEntity(weatherEntity)
                        .build())
                .toList();
    }


}
