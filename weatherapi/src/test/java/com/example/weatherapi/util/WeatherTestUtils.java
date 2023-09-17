package com.example.weatherapi.util;

import com.example.weatherapi.domain.weather.Weather;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
public class WeatherTestUtils {

    // Helper method to build weather data
    public static Weather.WeatherData buildWeatherData(float temperature, int weatherCode, float windSpeed, float windDirection, float precipitation) {
        return Weather.WeatherData.builder()
                .temperature(temperature)
                .weatherCode(weatherCode)
                .windSpeed(windSpeed)
                .windDirection(windDirection)
                .precipitation(precipitation)
                .build();
    }

    // Helper method to assert that the information part is always the same
    public static void assertWeatherInformation(Weather weather) {
        assertThat(weather.getInformation().getTemperature()).isEqualTo("Celsius");
        assertThat(weather.getInformation().getWindSpeed()).isEqualTo("m/s");
        assertThat(weather.getInformation().getPrecipitation()).isEqualTo("mm/hr - kg/m2/h");
        assertThat(weather.getInformation().getTime()).isEqualTo("UTC");
    }

    // Helper method to assert that the merged weather data is always the same for stockholm
    public static void assertWeatherDataMergedStockholm(Weather weather){
        assertThat(weather.getWeatherData().get(LocalDateTime.parse("2023-09-17T10:00")))
                .isEqualTo(buildWeatherData(14.8f, 2, 3.5f, 348.55f, 0.0f));
    }
    // Helper method to assert that the weather data is always the same for stockholm from smhi
    public static void assertWeatherDataSmhiStockholm(Weather weather){
        assertThat(weather.getWeatherData().get(LocalDateTime.parse("2023-09-17T10:00")))
                .isEqualTo(buildWeatherData(14.6f, 2, 3.4f, 347.0f, 0.0f));
    }

    // Helper method to assert that the weather data is always the same for stockholm from yr
    public static void assertWeatherDataYrStockholm(Weather weather){
        assertThat(weather.getWeatherData().get(LocalDateTime.parse("2023-09-17T10:00")))
                .isEqualTo(buildWeatherData(15.0f, 0, 3.6f, 350.1f, 0.0f));
    }

    // Helper method to assert that the merged weather data is always the same for gothenburg
    public static void assertWeatherDataMergedGothenburg(Weather weather){
        assertThat(weather.getWeatherData().get(LocalDateTime.parse("2023-09-17T15:00")))
                .isEqualTo(buildWeatherData(19.75f, 4, 3.8f, 80.95f, 0.0f));
    }

    // Helper method to assert that the weather data is always the same for gothenburg from smhi
    public static void assertWeatherDataSmhiGothenburg(Weather weather){
        assertThat(weather.getWeatherData().get(LocalDateTime.parse("2023-09-17T15:00")))
                .isEqualTo(buildWeatherData(19.8f, 4, 4.1f, 77.0f, 0.0f));
    }

    // Helper method to assert that the weather data is always the same for gothenburg from yr
    public static void assertWeatherDataYrGothenburg(Weather weather){
        assertThat(weather.getWeatherData().get(LocalDateTime.parse("2023-09-17T15:00")))
                .isEqualTo(buildWeatherData(19.7f, 0, 3.5f, 84.9f, 0.0f));
    }



}
