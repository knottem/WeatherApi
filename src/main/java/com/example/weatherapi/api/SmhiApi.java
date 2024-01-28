package com.example.weatherapi.api;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.domain.weather.WeatherSmhi;
import com.example.weatherapi.exceptions.ApiConnectionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * This class handles all the communication with the smhi api.<br>
 * It contains methods for fetching the weather from the smhi api and creating the Weather object for the location.
 * <p>
 * @author Erik Wallenius
 * @see <a href="https://github.com/knottem/WeatherApi">Repository Link</a>
 */
@Component
public class SmhiApi {

    ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
    private static final Logger LOG = LoggerFactory.getLogger(SmhiApi.class);
    private boolean isTestMode = false;

    /**
     * Sets the test mode to true or false.
     * @param isTestMode true if we are in test mode, false otherwise
     */
    public void setTestMode(boolean isTestMode) {
        this.isTestMode = isTestMode;
    }

    /**
     * Gets the weather from the smhi api. If the location is in the cache, the weather is returned from the cache.<br>
     * If the location is not in the cache, the weather is fetched from the smhi api and added to the cache before being returned.<br>
     * @param lon the longitude of the location
     * @param lat the latitude of the location
     * @param city the City object for the location
     * @return the Weather object for the location
     * @throws ApiConnectionException if the connection to the smhi api fails
     */

    public Weather getWeatherSmhi(double lon, double lat, City city) {
        LOG.info("Fetching weather data from the SMHI API...");
        WeatherSmhi weatherSmhi = fetchWeatherSmhi(lon, lat, city);
        return createWeather(lon, lat, city, weatherSmhi);
    }

    @Async
    public CompletableFuture<Weather> fetchWeatherSmhiAsync(double lon, double lat, City city) {
        return CompletableFuture.completedFuture(getWeatherSmhi(lon, lat, city));
    }

    /**
     * Fetches the weather from the smhi api or from a test file, depending on the test mode.
     * @return the WeatherSmhi object for the location
     */
    private WeatherSmhi fetchWeatherSmhi(double lon, double lat, City city) throws ApiConnectionException {
        try {
            if (isTestMode) {
                String cityName = city.getName().toLowerCase();
                LOG.info("Using test data for SMHI: {}", cityName);
                return mapper.readValue(getClass().getResourceAsStream("/weatherexamples/smhi/" +
                        mapper.readValue(getClass().getResourceAsStream("/weatherexamples/citiesexamples.json"), Map.class)
                                .get(cityName)), WeatherSmhi.class);
            } else {
                return mapper.readValue(new URL("https://opendata-download-metfcst.smhi.se/api/category/pmp3g/version/2/geotype/point/lon/"
                        + lon + "/lat/" + lat + "/data.json"), WeatherSmhi.class);
            }
        } catch (IOException e) {
            LOG.error("Could not connect to SMHI API", e);
            throw new ApiConnectionException("Could not connect to SMHI API, please contact the site administrator");
        }
    }

    /**
     * Creates the Weather object for the given location. <br>
     * Uses city information if provided, otherwise uses only coordinates.<br>
     * @return Constructed Weather object with relevant information.
     */
    private Weather createWeather(double lon, double lat, City city, WeatherSmhi weatherSmhi) {
        Weather weather;
        if (city == null) {
            weather = Weather.builder()
                    .message("Weather for location Lon: " + lon + " and Lat: " + lat)
                    .timestamp(ZonedDateTime.now(ZoneId.of("UTC")))
                    .build();
        } else {
            weather = Weather.builder()
                    .message("Weather for " + city.getName() + " with location Lon: " + city.getLon() + " and Lat: " + city.getLat())
                    .city(city)
                    .timestamp(ZonedDateTime.now(ZoneId.of("UTC")))
                    .build();
        }
        addWeatherDataSmhi(weather, weatherSmhi);
        return weather;
    }

    /**
     * Adds the weather data to the Weather object.
     * @param weather the Weather object for the location
     * @param weatherSmhi the WeatherSmhi object for the location
     */
    private void addWeatherDataSmhi(Weather weather, WeatherSmhi weatherSmhi) {
        weatherSmhi.timeSeries().forEach(t ->
            weather.addWeatherData(ZonedDateTime.of(t.validTime(), ZoneId.of("UTC")),
                    t.parameters().stream().filter(p -> p.name().equals("t"))
                            .map(p -> p.values().get(0)).findFirst().orElse(0f),
                    t.parameters().stream().filter(p -> p.name().equals("Wsymb2"))
                            .map(p -> p.values().get(0).intValue()).findFirst().orElse(0),
                    t.parameters().stream().filter(p -> p.name().equals("ws"))
                            .map(p -> p.values().get(0)).findFirst().orElse(0f),
                    t.parameters().stream().filter(p -> p.name().equals("wd"))
                            .map(p -> p.values().get(0)).findFirst().orElse(0f),
                    (t.parameters().stream().filter(p -> p.name().equals("pmin"))
                            .map(p -> p.values().get(0)).findFirst().orElse(0f)
                            + t.parameters().stream().filter(p -> p.name().equals("pmax"))
                            .map(p -> p.values().get(0)).findFirst().orElse(0f)) / 2));
    }
}