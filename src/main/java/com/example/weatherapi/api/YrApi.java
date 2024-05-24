package com.example.weatherapi.api;

import com.example.weatherapi.cache.CacheDB;
import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.domain.weather.WeatherYr;
import com.example.weatherapi.exceptions.ApiConnectionException;
import com.example.weatherapi.util.HttpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.example.weatherapi.util.WeatherCodeMapper.mapToWeatherCodeYR;
import static com.example.weatherapi.util.WeatherMapper.createBaseWeather;

//YrAPI class that handles all the communication with the yr api
@Component
public class YrApi {

    ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
    private static final Logger LOG = LoggerFactory.getLogger(YrApi.class);
    private final CacheManager cacheManager;
    private final CacheDB cacheDB;
    private final String cacheName;

    // Gets the domain and contact info from the application.properties file, contact info is required by the YR API
    @Value("${your.domain}")
    private String domain = "localhost";
    @Value("${contact.github}")
    private String contact;
    private boolean isTestMode = false;

    @Autowired
    public YrApi (CacheManager cacheManager, CacheDB cacheDB) {
        this.cacheManager = cacheManager;
        this.cacheDB = cacheDB;
        this.cacheName = "cache";
    }

    public void setTestMode(boolean isTestMode) {
        this.isTestMode = isTestMode;
    }

    private URL getUrlYr(double lon, double lat) throws IOException {
        return new URL("https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=" + lat + "&lon=" + lon);
    }

    @Async
    public CompletableFuture<Weather> fetchWeatherYrAsync(City city) {
        return CompletableFuture.completedFuture(getWeatherYr(city.getLon(), city.getLat(), city));
    }

    public Weather getWeatherYr(double lon, double lat, City city) {
        String key = city.getName().toLowerCase() + "yr";
        Weather weatherFromCache = Objects.requireNonNull(cacheManager.getCache(cacheName))
                .get(key, Weather.class);
        if(weatherFromCache != null) {
            LOG.info("Cache hit for City: {} in the cache, returning cached data for yr", city.getName());
            return weatherFromCache;
        }

        Weather weatherFromCacheDB = cacheDB.getWeatherFromCache(city.getName(), false, true, false);
        if(weatherFromCacheDB != null) {
            Objects.requireNonNull(cacheManager.getCache(cacheName)).put(key, weatherFromCacheDB);
            return weatherFromCacheDB;
        }

        LOG.info("Fetching weather data from the YR API...");
        WeatherYr weatherYr = fetchWeatherYr(lon, lat, city);
        Weather weather = createBaseWeather(lon, lat, city, "YR");
        addWeatherDataYr(weather, weatherYr);
        cacheDB.save(weather, false, true, false);
        Objects.requireNonNull(cacheManager.getCache(cacheName)).put(key, weather);
        return weather;
    }

    // The YR API requires a custom User-Agent header, otherwise it will return 403 Forbidden. So we need both our domain and contact info which is provided by the application.properties file.
    private WeatherYr fetchWeatherYr(double lon, double lat, City city) {
        try {
            if (isTestMode) {
                return mapper.readValue(getClass().getResourceAsStream("/weatherexamples/yr/" +
                                mapper.readValue(getClass().getResourceAsStream("/weatherexamples/citiesexamples.json"), Map.class).get(city.getName().toLowerCase())),
                        WeatherYr.class);
            } else {
                URI uri = getUrlYr(lon, lat).toURI();
                Map<String, String> headers = Map.of(
                        "User-Agent", domain,
                        "sitename", contact
                );
                HttpResponse<String> response = HttpUtil.getContentFromUrlWithHeaders(uri, headers);

                if (response.statusCode() == 403) {
                    throw new ApiConnectionException("Forbidden: Custom User-Agent is required.");
                }

                if (response.statusCode() != 200) {
                    throw new ApiConnectionException("Could not connect to YR API, please contact the site administrator");
                }

                return mapper.readValue(response.body(), WeatherYr.class);
            }
        } catch (Exception e){
            LOG.warn("Could not connect to YR API", e);
            Thread.currentThread().interrupt();
            throw new ApiConnectionException("Could not connect to YR API, please contact the site administrator");
        }
    }

    private void addWeatherDataYr(Weather weather, WeatherYr weatherYr) {
        weatherYr.properties().timeseries().forEach(t ->
                weather.addWeatherData(t.time(),
                        t.data().instant().details().air_temperature(),
                        mapToWeatherCodeYR(t),
                        t.data().instant().details().wind_speed(),
                        t.data().instant().details().wind_from_direction(),
                        t.data().instant().details().precipitation_amount()));
    }

}
