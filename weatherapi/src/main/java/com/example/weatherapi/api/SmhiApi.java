package com.example.weatherapi.api;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.WeatherCache;
import com.example.weatherapi.domain.weather.WeatherSmhi;
import com.example.weatherapi.exceptions.ApiConnectionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SmhiApi {

    private final Logger logger = LoggerFactory.getLogger(SmhiApi.class);
    ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
    private final int CACHE_TIME_IN_HOURS = 3;

    private final Map<String, WeatherCache> cache = new HashMap<>();


    private URL getUrl(double lon, double lat) throws IOException {
        return new URL("https://opendata-download-metfcst.smhi.se/api/category/pmp3g/version/2/geotype/point/lon/"
                + lon + "/lat/" + lat + "/data.json");
    }

    //might change to cache in database
    private WeatherSmhi getWeatherSmhi(double lon, double lat){
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
            WeatherSmhi weatherSmhi = mapper.readValue(getUrl(lon, lat), WeatherSmhi.class);
            entry = new WeatherCache(weatherSmhi);
            cache.put(key, entry);
            return weatherSmhi;
        } catch (IOException e){
            throw new ApiConnectionException("Could not connect to SMHI API");
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

        WeatherSmhi weatherSmhi = getWeatherSmhi(lon, lat);

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
