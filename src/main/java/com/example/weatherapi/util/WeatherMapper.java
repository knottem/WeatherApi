package com.example.weatherapi.util;

import com.example.weatherapi.domain.city.City;
import com.example.weatherapi.domain.entities.CityEntity;
import com.example.weatherapi.domain.entities.LatestWeatherApiEntity;
import com.example.weatherapi.domain.entities.WeatherDataEntity;
import com.example.weatherapi.domain.entities.WeatherEntity;
import com.example.weatherapi.domain.weather.Weather;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.weatherapi.util.CityMapper.toModel;

/**
 * Utility class for converting between weather domain objects and
 * persistence entities.
 * <p>
 * The mapper also provides helper methods for creating base weather objects
 * and restoring weather source information from persisted API metadata.
 *
 * @author Erik Wallenius
 * @see <a href="https://github.com/knottem/WeatherApi">Repository Link</a>
 */
public class WeatherMapper {

    private WeatherMapper(){
        throw new IllegalStateException("Utility class");
    }

    /**
     * Converts a Weather domain object into a WeatherEntity for persistence.
     * <p>
     * Each weather data entry is converted into a WeatherDataEntity and
     * associated with the created WeatherEntity.
     *
     * @param weather the weather domain object to convert
     * @param cityEntity the persisted city entity associated with the weather data
     * @return the converted WeatherEntity
     */
    public static WeatherEntity convertToWeatherEntity(Weather weather, CityEntity cityEntity) {
        WeatherEntity weatherEntity = WeatherEntity.builder()
                .message(weather.getMessage())
                .timeStamp(weather.getTimestamp())
                .city(cityEntity)
                .build();

        List<WeatherDataEntity> weatherDataEntities = weather.getWeatherData().entrySet().stream()
                .map(entry -> WeatherDataEntity.builder()
                        .validTime(entry.getKey())
                        .temperature(entry.getValue().getTemperature())
                        .weatherCode(entry.getValue().getWeatherCode())
                        .windSpeed(entry.getValue().getWindSpeed())
                        .windDirection(entry.getValue().getWindDirection())
                        .precipitation(entry.getValue().getPrecipitation())
                        .humidity(entry.getValue().getHumidity())
                        .weatherEntity(weatherEntity)
                        .build())
                .toList();

        weatherEntity.setWeatherDataList(weatherDataEntities);

        return weatherEntity;
    }

    /**
     * Converts a persisted WeatherEntity into a Weather domain object.
     *
     * @param weatherEntity the persisted weather entity to convert
     * @return the converted Weather object
     */
    public static Weather convertToWeather(WeatherEntity weatherEntity) {
        return Weather.builder()
                .message(weatherEntity.getMessage())
                .timestamp(weatherEntity.getTimeStamp())
                .city(toModel(weatherEntity.getCity()))
                .weatherData(convertToWeatherDataMap(weatherEntity.getWeatherDataList()))
                .build();
    }

    /**
     * Converts a persisted WeatherEntity into a Weather domain object and
     * restores information about which weather APIs contributed to the data.
     *
     * @param weatherEntity the persisted weather entity to convert
     * @param latestWeatherApiEntity entity containing the APIs associated
     *                               with the persisted weather data
     * @return the converted Weather object including its weather sources
     */
    public static Weather convertToWeather(WeatherEntity weatherEntity, LatestWeatherApiEntity latestWeatherApiEntity) {
        return Weather.builder()
                .message(weatherEntity.getMessage())
                .timestamp(weatherEntity.getTimeStamp())
                .city(toModel(weatherEntity.getCity()))
                .weatherData(convertToWeatherDataMap(weatherEntity.getWeatherDataList()))
                .weatherSources(getWeatherSources(latestWeatherApiEntity))
                .build();
    }

    /**
     * Converts a list of persisted weather data entities into a map of
     * weather data keyed by its valid timestamp.
     * <p>
     * Entries are sorted chronologically before being inserted into the
     * returned map.
     *
     * @param weatherDataEntities the persisted weather data entries to convert
     * @return a timestamp-ordered map containing the converted weather data
     */
    private static Map<ZonedDateTime, Weather.WeatherData> convertToWeatherDataMap(List<WeatherDataEntity> weatherDataEntities) {
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
                                .humidity(entity.getHumidity())
                                .build(),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    /**
     * Creates a base Weather object for weather data returned by a specific API.
     * <p>
     * If a city is supplied, the city and its coordinates are included in the
     * resulting object. Otherwise, the supplied longitude and latitude are used
     * directly in the generated message.
     * <p>
     * The timestamp is set to the current UTC time and the supplied API is added
     * as the weather source.
     *
     * @param lon longitude of the requested location
     * @param lat latitude of the requested location
     * @param city the city associated with the request, or {@code null} when
     *             requesting weather directly by coordinates
     * @param api the name of the API providing the weather data
     * @return a base Weather object containing request metadata
     */
    public static Weather createBaseWeather(double lon, double lat, City city, String api) {
        Weather weather;
        if (city == null) {
            weather = Weather.builder()
                    .message("Weather for location Lon: " + lon + " and Lat: " + lat + " from: " + api)
                    .timestamp(ZonedDateTime.now(ZoneOffset.UTC))
                    .build();
        } else {
            weather = Weather.builder()
                    .message("Weather for " + city.getName() + " with location Lon: " + city.getLon() + " and Lat: " + city.getLat() + " from: " + api)
                    .city(city)
                    .timestamp(ZonedDateTime.now(ZoneOffset.UTC))
                    .build();
        }
        return weather;
    }

    /**
     * Returns the weather APIs marked as contributors in the supplied entity.
     *
     * @param entity the entity containing persisted API source information
     * @return a list containing the names of the APIs associated with the weather data
     */
    private static List<String> getWeatherSources(LatestWeatherApiEntity entity) {
        List<String> apis = new ArrayList<>();
        if (entity.isFmi()) apis.add("FMI");
        if (entity.isYr()) apis.add("YR");
        if (entity.isSmhi()) apis.add("SMHI");
        return apis;
    }

}