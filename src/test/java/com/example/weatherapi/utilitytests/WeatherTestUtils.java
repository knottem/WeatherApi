package com.example.weatherapi.utilitytests;

import com.example.weatherapi.domain.weather.Weather;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class contains helper methods for the Weather tests.
 * It includes methods for building weather data, and asserting that the weather data is correct.
 * <p>
 * @author Erik Wallenius
 * @see <a href="https://github.com/knottem/WeatherApi">Repository Link</a>
 */
public class WeatherTestUtils {

    /**
     * Builds a WeatherData object with the given parameters.
     * @return a WeatherData object constructed with the given weather attributes
     */
    public static Weather.WeatherData buildWeatherData(float temperature, int weatherCode, float windSpeed, float windDirection, float precipitation, float humidity) {
        return Weather.WeatherData.builder()
                .temperature(temperature)
                .weatherCode(weatherCode)
                .windSpeed(windSpeed)
                .windDirection(windDirection)
                .precipitation(precipitation)
                .humidity(humidity)
                .build();
    }

    public static Weather.WeatherData buildWeatherDataWithoutWind(float temperature, float precipitation, float humidity) {
        return Weather.WeatherData.builder()
                .temperature(temperature)
                .precipitation(precipitation)
                .humidity(humidity)
                .weatherCode(-1)
                .windSpeed(-99f)
                .windDirection(-99f)
                .build();
    }

    /**
     * Asserts that the information part of the Weather object is always the same.
     * @param weather the Weather object to be tested
     */
    public static void assertWeatherInformation(Weather weather) {
        assertThat(weather.getInformation().getTemperature()).isEqualTo("Celsius");
        assertThat(weather.getInformation().getWindSpeed()).isEqualTo("m/s");
        assertThat(weather.getInformation().getPrecipitation()).isEqualTo("mm/hr - kg/m2/h");
        assertThat(weather.getInformation().getTime()).isEqualTo("UTC");
    }

    /**
     * Asserts that the merged weather data for Stockholm is always the same.
     * @param weather the Weather object to be tested
     */
    public static void assertWeatherDataMergedStockholm(Weather weather){
        // SMHI DATA = 20.4, 1, 3.7, 196.0, 0.0
        // YR DATA = 21.4, 1, 3.8, 187.0, 0.0
        // FMI DATA = 17.2, ,    ,      , 0.0 -- ignored in test due to using old xml file which FMI has changed
        assertThat(weather.getWeatherData().get(ZonedDateTime.parse("2024-05-15T17:00:00Z")))
                .isEqualTo(buildWeatherData(20.9f, 1, 3.8f, 191.5f, 0.0f, 50.0f));
    }

    /**
     * Asserts that the weather data for Stockholm from SMHI is always the same.
     * @param weather the Weather object to be tested
     */
    public static void assertWeatherDataSmhiStockholm(Weather weather){
        assertThat(weather.getWeatherData().get(ZonedDateTime.parse("2024-05-15T17:00:00Z")))
                .isEqualTo(buildWeatherData(20.4f, 1, 3.7f, 196.0f, 0.0f, 52.0f));
    }


    /**
     * Asserts that the weather data for Stockholm from YR is always the same.
     * @param weather the Weather object to be tested
     */
    public static void assertWeatherDataYrStockholm(Weather weather){
        assertThat(weather.getWeatherData().get(ZonedDateTime.parse("2024-05-15T17:00:00Z")))
                .isEqualTo(buildWeatherData(21.4f, 1, 3.8f, 187.0f, 0.0f, 48.1f));
    }

    /**
     * Asserts that the weather data for raining is always the same.
     * @param weather the Weather object to be tested
     */
    public static void assertWeatherDataYrRain(Weather weather){
        assertThat(weather.getWeatherData().get(ZonedDateTime.parse("2024-09-27T13:00:00Z")))
                .isEqualTo(buildWeatherData(12.3f, 18, 3.4f, 332.7f, 0.1f, 92.0f));
    }

    /**
     * Asserts that the weather data for raining is always the same at a later time.
     * @param weather the Weather object to be tested
     */
    public static void assertWeatherDataYrRainLater(Weather weather){
        assertThat(weather.getWeatherData().get(ZonedDateTime.parse("2024-10-06T18:00:00Z")))
                .isEqualTo(buildWeatherData(9.6f, 19, 2.8f, 195.0f, 1.8f, 90.1f));
    }

    /**
     * Asserts that the merged weather data for Gothenburg is always the same.
     * @param weather the Weather object to be tested
     */
    public static void assertWeatherDataMergedGothenburg(Weather weather){
        assertThat(weather.getWeatherData().get(ZonedDateTime.parse("2023-09-17T15:00:00Z")))
                .isEqualTo(buildWeatherData(19.8f, 4, 3.8f, 80.9f, 0.0f, 55.0f));
    }

    /**
     * Asserts that the weather data for Gothenburg from SMHI is always the same.
     * @param weather the Weather object to be tested
     */
    public static void assertWeatherDataSmhiGothenburg(Weather weather){
        assertThat(weather.getWeatherData().get(ZonedDateTime.parse("2023-09-17T15:00:00Z")))
                .isEqualTo(buildWeatherData(19.8f, 4, 4.1f, 77.0f, 0.0f, 55.0f));
    }

    /**
     * Asserts that the weather data for Gothenburg from YR is always the same.
     * @param weather the Weather object to be tested
     */
    public static void assertWeatherDataYrGothenburg(Weather weather){
        assertThat(weather.getWeatherData().get(ZonedDateTime.parse("2023-09-17T15:00:00Z")))
                .isEqualTo(buildWeatherData(19.7f, 3, 3.5f, 84.9f, 0.0f, 55.0f));
    }

    public static void assertWeatherDataFmiRagsved(Weather weather){
        assertThat(weather.getWeatherData().get(ZonedDateTime.parse("2024-09-18T17:00:00Z")))
                .isEqualTo(buildWeatherDataWithoutWind(14.5f, 0.0f, 80.6f));
    }

    public static void assertWeatherDataFmiStockholm(Weather weather){
        assertThat(weather.getWeatherData().get(ZonedDateTime.parse("2024-09-18T17:00:00Z")))
                .isEqualTo(buildWeatherDataWithoutWind(14.5f, 0.0f, 81.6f));
    }
}
