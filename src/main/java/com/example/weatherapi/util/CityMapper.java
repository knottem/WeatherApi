package com.example.weatherapi.util;

import com.example.weatherapi.domain.city.City;
import com.example.weatherapi.domain.entities.CityEntity;

/**
 * This class contains methods for converting between City and CityEntity objects.
 *
 * @author Erik Wallenius
 * @see <a href="https://github.com/knottem/WeatherApi">Repository Link</a>
 */
public class CityMapper {

    private CityMapper(){
        throw new IllegalStateException("Utility class");
    }


    /**
     * Convert a CityEntity object to a City object.
     *
     * @param entity the CityEntity object to convert
     * @return the converted City object
     */
    public static City toModel(CityEntity entity) {
        return City.builder()
                .name(entity.getName())
                .lat(entity.getLat())
                .lon(entity.getLon())
                .build();
    }

    /**
     * Convert a City object to a CityEntity object.
     *
     * @param city the City object to convert
     * @return the converted CityEntity object
     */
    public static CityEntity toEntity(City city) {
        return CityEntity.builder()
                .name(city.getName())
                .lat(city.getLat())
                .lon(city.getLon())
                .build();
    }
}