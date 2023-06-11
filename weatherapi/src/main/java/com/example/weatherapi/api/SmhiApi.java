package com.example.weatherapi.api;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.domain.weather.WeatherCache;
import com.example.weatherapi.domain.weather.WeatherSmhi;
import com.example.weatherapi.domain.weather.WeatherYr;
import com.example.weatherapi.exceptions.ApiConnectionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;


//TODO - Split this class into two classes, one for each API or make it more generic
@Component
public class SmhiApi {

    private final Logger logger;
    ObjectMapper mapper;
    private final Map<String, WeatherCache> cache;

    public SmhiApi() {
        this.mapper = JsonMapper.builder().findAndAddModules().build();
        this.logger = LoggerFactory.getLogger(SmhiApi.class);
        this.cache = new HashMap<>();
    }

    @Value("${your.domain}")
    private String domain = "localhost";
    @Value("${contact.github}")
    private String contact;
    @Value("${cache.time.in.hours}")
    private int CACHE_TIME_IN_HOURS;

    private URL getUrlSmhi(double lon, double lat) throws IOException {
        return new URL("https://opendata-download-metfcst.smhi.se/api/category/pmp3g/version/2/geotype/point/lon/"
                + lon + "/lat/" + lat + "/data.json");
    }

    private URL getUrlYr(double lon, double lat) throws IOException {
        return new URL("https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=" + lat + "&lon=" + lon);
    }

    public Weather getWeatherByCity(City cityObject) {
        logger.info("getWeather called with city: " + cityObject.getName() + ", Lon: " + cityObject.getLon() + ", Lat: " + cityObject.getLat());
        return getWeatherSmhi(cityObject.getLon(), cityObject.getLat(), cityObject);
    }

    public Weather getWeatherbyCoordinates(double lon, double lat) {
        logger.info("getWeather called with coordinates: Lon: " + lon + ", Lat: " + lat);
        return getWeatherSmhi(lon, lat, null);
    }

    public Weather getWeatherYrByCity(double lon, double lat, City cityObject) {
        logger.info("getWeatherYr called with coordinates: Lon: " + lon + ", Lat: " + lat);
        return getWeatherYr(lon, lat, cityObject);
    }

    public Weather getWeatherYrByCoordinates(double lon, double lat) {
        logger.info("getWeatherYr called with coordinates: Lon: " + lon + ", Lat: " + lat);
        return getWeatherYr(lon, lat, null);
    }

    //Merges the weather from SMHI and YR, for now it only merges the temperatures
    public Weather mergeWeather(City cityObject) {
        Weather weatherYr = getWeatherYr(cityObject.getLon(), cityObject.getLat(), cityObject);
        Weather weatherSmhi = getWeatherSmhi(cityObject.getLon(), cityObject.getLat(), cityObject);

        Map<LocalDateTime, Float> smhiTemperatures = weatherSmhi.getTemperatures();
        Map<LocalDateTime, Float> yrTemperatures = weatherYr.getTemperatures();

        Map<LocalDateTime, Float> mergedTemperatures = new TreeMap<>();

        mergedTemperatures.putAll(smhiTemperatures);

        for (Map.Entry<LocalDateTime, Float> entry : yrTemperatures.entrySet()) {
            LocalDateTime key = entry.getKey();
            Float yrValue = entry.getValue();
            if (mergedTemperatures.containsKey(key)) {
                float smhiValue = mergedTemperatures.get(key);
                float median = (smhiValue + yrValue) / 2;
                mergedTemperatures.put(key, median);
            } else {
                mergedTemperatures.put(key, yrValue);
            }
        }

        Weather mergedWeather = Weather.builder()
                .message("Merged weather for " + cityObject.getName() + " with location Lon: " + cityObject.getLon() + " and Lat: " + cityObject.getLat()).build();
        mergedWeather.setTemperatures(mergedTemperatures);
        return mergedWeather;
    }

    //The Smhi API doesnt need custom headers so this method is a lot simpler than the one in YrApi
    private Weather getWeatherSmhi(double lon, double lat, City cityObject) {
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
            if(weatherSmhi == null) {
                throw new ApiConnectionException("Could not connect to SMHI API, please contact the site administrator");
            }

            Weather weather;
            if(cityObject == null){
                weather = Weather.builder()
                        .message("Weather for location Lon: " + lon + " and Lat: " + lat).build();
            } else {
                weather = Weather.builder()
                        .message("Weather for " + cityObject.getName() + " with location Lon: " + cityObject.getLon() + " and Lat: " + cityObject.getLat()).build();
            }
            weatherSmhi.timeSeries().stream().forEach(t ->
                    weather.addTemperature(t.validTime(), t.parameters().stream()
                            .filter(p -> p.name().equals("t"))
                            .map(p -> p.values().get(0))
                            .findFirst()
                            .orElse(0f)));
            entry = new WeatherCache(weather);
            cache.put(key, entry);
            return weather;
        } catch (IOException e){
            e.printStackTrace();
            throw new ApiConnectionException("Could not connect to SMHI API, please contact the site administrator");
        }
    }

    // The YR API requires a custom User-Agent header, otherwise it will return 403 Forbidden. So we need both our domain and contact info which is provided by the application.properties file.
    private Weather getWeatherYr(double lon, double lat, City cityObject) {
        String key = lon + "," + lat + ",yr";
        WeatherCache entry = cache.get(key);
        if(entry != null && entry.isValid(CACHE_TIME_IN_HOURS)) {
            logger.info("Cache hit for key: " + key + ", returning cached data");
            return entry.getWeather();
        }  else if(entry != null) {
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
                    .header("User-Agent", domain)
                    .header("sitename", contact)
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 403) {
                throw new ApiConnectionException("Forbidden: Custom User-Agent is required.");
            }

            WeatherYr weatherYr = mapper.readValue(response.body(), WeatherYr.class);
            if(weatherYr == null) {
                throw new ApiConnectionException("Could not connect to YR API, please contact the site administrator");
            }
            Weather weather;
            if(cityObject == null){
                weather = Weather.builder()
                        .message("Weather for location Lon: " + lon + " and Lat: " + lat).build();
            } else {
                weather = Weather.builder()
                        .message("Weather for " + cityObject.getName() + " with location Lon: " + cityObject.getLon() + " and Lat: " + cityObject.getLat()).build();
            }
            weatherYr.properties().timeseries().stream().forEach(t ->
                    weather.addTemperature(t.time(), (float) t.data().instant().details().air_temperature()));
            entry = new WeatherCache(weather);
            cache.put(key, entry);
            return weather;
        } catch (Exception e){
            e.printStackTrace();
            throw new ApiConnectionException("Could not connect to YR API, please contact the site administrator");
        }
    }


}
