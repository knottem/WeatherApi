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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;

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

    //The Smhi API doesnt need custom headers so this method is a lot simpler than the one in YrApi
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

    // The YR API requires a custom User-Agent header, otherwise it will return 403 Forbidden. So we need both our domain and contact info which is provided by the application.properties file.
    //TODO - Change to private when done testing and compile the results from smhi and yr to one Weather object
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
                    .header("User-Agent", domain)
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
