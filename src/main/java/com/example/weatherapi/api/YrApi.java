package com.example.weatherapi.api;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.domain.weather.WeatherYr;
import com.example.weatherapi.exceptions.ApiConnectionException;
import com.example.weatherapi.util.Cache;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Map;

//YrAPI class that handles all the communication with the yr api
@Component
public class YrApi {

    ObjectMapper mapper;
    private Cache cache;
    private static final Logger logger = LoggerFactory.getLogger(YrApi.class);

    @Autowired
    public YrApi(Cache cache) {
        this.cache = cache;
        this.mapper = JsonMapper.builder().findAndAddModules().build();
    }
    // Gets the domain and contact info from the application.properties file, contact info is required by the YR API
    @Value("${your.domain}")
    private String domain = "localhost";
    @Value("${contact.github}")
    private String contact;
    private boolean isTestMode = false;
    public void setTestMode(boolean isTestMode) {
        this.isTestMode = isTestMode;
    }

    // Method that creates the url for the yr api
    private URL getUrlYr(double lon, double lat) throws IOException {
        return new URL("https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=" + lat + "&lon=" + lon);
    }

    // The YR API requires a custom User-Agent header, otherwise it will return 403 Forbidden. So we need both our domain and contact info which is provided by the application.properties file.
    //TODO: Add support for weathercodes
    public Weather getWeatherYr(double lon, double lat, City city) {
        String key = lon + ":" + lat + ":yr";
        // Checks if the weather is in the cache, if it is it returns it
        Weather weatherFromCache = cache.getWeatherFromCache(key);
        if(weatherFromCache != null) {
            return weatherFromCache;
        }
        try {
            WeatherYr weatherYr;
            if(isTestMode){
                Map<String, String> cityMap = mapper.readValue(getClass().getResourceAsStream("/weatherexamples/citiesexamples.json"), Map.class);
                String cityName = city.getName().toLowerCase();
                logger.info("Using test data for YR: {}", cityName);
                weatherYr = mapper.readValue(getClass().getResourceAsStream("/weatherexamples/yr/" + cityMap.get(cityName)), WeatherYr.class);
            } else {

                HttpClient httpClient = HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .build();

                // Adds User-Agent and sitename to header since YR requires it
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(getUrlYr(lon, lat).toURI())
                        .header("User-Agent", domain)
                        .header("sitename", contact)
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 403) {
                    throw new ApiConnectionException("Forbidden: Custom User-Agent is required.");
                }
                weatherYr = mapper.readValue(response.body(), WeatherYr.class);
                if (weatherYr == null) {
                    throw new ApiConnectionException("Could not connect to YR API, please contact the site administrator");
                }
            }
            // Creates a new weather object and adds the location and message to it
            Weather weather;
            if(city == null){
                weather = Weather.builder()
                        .message("Weather for location Lon: " + lon + " and Lat: " + lat)
                        .timeStamp(LocalDateTime.now())
                        .build();
            } else {
                weather = Weather.builder()
                        .message("Weather for " + city.getName() + " with location Lon: " + city.getLon() + " and Lat: " + city.getLat())
                        .timeStamp(LocalDateTime.now())
                        .build();
            }
            addWeatherDataYr(weather, weatherYr);
            cache.save(key, weather);
            return weather;
        } catch (Exception e){
            throw new ApiConnectionException("Could not connect to YR API, please contact the site administrator");
        }
    }

    private void addWeatherDataYr(Weather weather, WeatherYr weatherYr) {
        weatherYr.properties().timeseries().forEach(t ->
                weather.addWeatherData(
                        t.time(),
                        t.data().instant().details().air_temperature(),
                        0,
                        t.data().instant().details().wind_speed(),
                        t.data().instant().details().wind_from_direction(),
                        t.data().instant().details().precipitation_amount()));
    }
}
