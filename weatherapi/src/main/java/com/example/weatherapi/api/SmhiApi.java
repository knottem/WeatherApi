package com.example.weatherapi.api;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.WeatherCache;
import com.example.weatherapi.domain.weather.WeatherSmhi;
import com.example.weatherapi.domain.weather.WeatherYr;
import com.example.weatherapi.exceptions.ApiConnectionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;

public class SmhiApi {

    private final Logger logger;
    ObjectMapper mapper;
    private final Map<String, WeatherCache> cache;

    public SmhiApi() {
        this.mapper = JsonMapper.builder().findAndAddModules().build();
        this.logger = LoggerFactory.getLogger(SmhiApi.class);
        this.cache = new HashMap<>();
    }


    //TODO: fix Values not working,now using default values, do not send request to YR before this is fixed
    @Value("${your.domain}")
    private String domain = "localhost";

    @Value("${contact.github}")
    private String contact;

    @Value("${cache.time.in.hours}")
    private int CACHE_TIME_IN_HOURS = 3;

    private URL getUrlSmhi(double lon, double lat) throws IOException {
        return new URL("https://opendata-download-metfcst.smhi.se/api/category/pmp3g/version/2/geotype/point/lon/"
                + lon + "/lat/" + lat + "/data.json");
    }

    private URL getUrlYr(double lon, double lat) throws IOException {
        return new URL("https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=" + lat + "&lon=" + lon);
    }

    //might change to cache in database
    private WeatherSmhi getWeatherFromSmhi(double lon, double lat){
        String key = lon + "," + lat;
        WeatherCache entry = cache.get(key);
        if(entry != null && entry.isValid(CACHE_TIME_IN_HOURS)) {
            logger.info("Cache hit for key: " + key + ", returning cached data");
            return entry.getWeather();
        } else if(entry != null) {
            logger.info("Cache expired for key: " + key + ", fetching new data");
        } else {
            logger.info("Cache doesn't exist for key: " + key + ", fetching new data");
        }
        try {
            WeatherSmhi weatherSmhi = mapper.readValue(getUrlSmhi(lon, lat), WeatherSmhi.class);
            entry = new WeatherCache(weatherSmhi);
            cache.put(key, entry);
            return weatherSmhi;
        } catch (IOException e){
            e.printStackTrace();
            throw new ApiConnectionException("Could not connect to SMHI API");
        }
    }

    public WeatherYr getWeatherYr(double lon, double lat){
        String key = lon + "," + lat;
        WeatherCache entry = cache.get(key);
        if(entry != null && entry.isValid(CACHE_TIME_IN_HOURS)) {
            logger.info("Cache hit for key: " + key + ", returning cached data");
            return entry.getWeatherYr();
        } else if(entry != null) {
            logger.info("Cache expired for key: " + key + ", fetching new data");
        } else {
            logger.info("Cache doesn't exist for key: " + key + ", fetching new data");
        }
        try {

            HttpClient httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(getUrlYr(lon, lat).toURI())
                    .header("User-Agent", domain) // Replace "Custom User-Agent" with your desired value
                    .header("sitename", contact)
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 403) {
                throw new ApiConnectionException("Forbidden: Custom User-Agent is required.");
            }
            WeatherYr weatherYr = mapper.readValue(response.body(), WeatherYr.class);
            entry = new WeatherCache(weatherYr);
            cache.put(key, entry);
            return weatherYr;
        } catch (Exception e){
            e.printStackTrace();
            throw new ApiConnectionException("Could not connect to YR API");
        }
    }

    public ResponseEntity<Object> getWeatherByCity(City city) {
        logger.info("getWeather called with city: " + city.getName());
        logger.info("City found: " + city);

        // Get the temperatures from the API
        Map<String, Float> temps = getTempsSmhi(city.getLon(), city.getLat());

        // Creates a map with the data to return to the client
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("city", city.getName());
        data.put("lon", city.getLon());
        data.put("lat", city.getLat());
        data.put("temperatures", temps);
        return ResponseEntity.ok(data);
    }

    private Map<String, Float> getTempsSmhi(double lon, double lat) {

        WeatherSmhi weatherSmhi = getWeatherFromSmhi(lon, lat);

        List<LocalDateTime> validTimes = weatherSmhi.timeSeries().stream()
                .map(WeatherSmhi.TimeSerie::validTime).toList();

        List<Float> temperatures = weatherSmhi.timeSeries().stream()
                .flatMap(t -> t.parameters().stream())
                .filter(p -> p.name().equals("t"))
                .map(p -> p.values().get(0)).toList();

        Map<String, Float> temps = new LinkedHashMap<>();

        for (int i = 0; i < validTimes.size() && i < temperatures.size(); i++) {
            temps.put(validTimes.get(i).toString(), temperatures.get(i));
        }
        return temps;
    }
}
