package com.example.weatherapi.api;

import com.example.weatherapi.cache.CacheDB;
import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.domain.weather.WeatherSmhi;
import com.example.weatherapi.exceptions.ApiConnectionException;
import com.example.weatherapi.util.HttpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.example.weatherapi.util.HttpUtil.getContentFromUrl;
import static com.example.weatherapi.util.WeatherMapper.createBaseWeather;

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
    private final CacheManager cacheManager;
    private final CacheDB cacheDB;
    private boolean isTestMode = false;
    private final String cacheName;
    private static final int CONNECTION_TIMEOUT = 5000; // 5 seconds
    private static final int READ_TIMEOUT = 10000; // 10 seconds

    @Autowired
    public SmhiApi (CacheManager cacheManager, CacheDB cacheDB) {
        this.cacheManager = cacheManager;
        this.cacheDB = cacheDB;
        this.cacheName = "cache";
    }

    /**
     * Sets the test mode to true or false.
     * @param isTestMode true if we are in test mode, false otherwise
     */
    public void setTestMode(boolean isTestMode) {
        this.isTestMode = isTestMode;
    }

    private URL getUrlSmhi(double lon, double lat) throws IOException {
        return new URL("https://opendata-download-metfcst.smhi.se/api/category/pmp3g/version/2/geotype/point/lon/"
                + lon + "/lat/" + lat + "/data.json");
    }

    @Async
    public CompletableFuture<Weather> fetchWeatherSmhiAsync(City city) {
        return CompletableFuture.completedFuture(getWeatherSmhi(city.getLon(), city.getLat(), city));
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
        String key = city.getName().toLowerCase() + "smhi";
        Weather weatherFromCache = Objects.requireNonNull(cacheManager.getCache(cacheName))
                .get(key, Weather.class);
        if(weatherFromCache != null) {
            LOG.info("Cache hit for City: {} in the cache, returning cached data for smhi", city.getName());
            return weatherFromCache;
        }

        Weather weatherFromCacheDB = cacheDB.getWeatherFromCache(city.getName(), true, false, false);
        if(weatherFromCacheDB != null) {
            Objects.requireNonNull(cacheManager.getCache(cacheName)).put(key, weatherFromCacheDB);
            return weatherFromCacheDB;
        }

        LOG.info("Fetching weather data from the SMHI API...");
        WeatherSmhi weatherSmhi = fetchWeatherSmhi(lon, lat, city);
        Weather weather = createBaseWeather(lon, lat, city, "SMHI");
        addWeatherDataSmhi(weather, weatherSmhi);
        cacheDB.save(weather, true, false, false);
        Objects.requireNonNull(cacheManager.getCache(cacheName)).put(key, weather);
        return weather;
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
                URI uri = getUrlSmhi(lon, lat).toURI();
                HttpResponse<String> response = HttpUtil.getContentFromUrl(uri);

                if (response.statusCode() != 200) {
                    throw new ApiConnectionException("Error: Received status code " + response.statusCode());
                }

                return mapper.readValue(response.body(), WeatherSmhi.class);
            }
        } catch (Exception e) {
            LOG.error("Could not connect to SMHI API");
            throw new ApiConnectionException("Could not connect to SMHI API, please contact the site administrator");
        }
    }


    /**
     * Adds the weather data to the Weather object.
     * @param weather the Weather object for the location
     * @param weatherSmhi the WeatherSmhi object for the location
     */
    private void addWeatherDataSmhi(Weather weather, WeatherSmhi weatherSmhi) {
        weatherSmhi.timeSeries().forEach(t ->
            weather.addWeatherData(t.validTime(),
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