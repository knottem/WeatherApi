package com.example.weatherapi.api;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.domain.weather.WeatherYr;
import com.example.weatherapi.exceptions.ApiConnectionException;
import com.example.weatherapi.util.Cache;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class YrApi {

    ObjectMapper mapper;

    public YrApi() {
        this.mapper = JsonMapper.builder().findAndAddModules().build();
    }
    @Value("${your.domain}")
    private String domain = "localhost";
    @Value("${contact.github}")
    private String contact;
    @Value("${cache.time.in.hours}")
    private int CACHE_TIME_IN_HOURS;

    private URL getUrlYr(double lon, double lat) throws IOException {
        return new URL("https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=" + lat + "&lon=" + lon);
    }

    // The YR API requires a custom User-Agent header, otherwise it will return 403 Forbidden. So we need both our domain and contact info which is provided by the application.properties file.
    public Weather getWeatherYr(double lon, double lat, City cityObject) {
        String key = lon + "," + lat + ",yr";
        Weather weatherFromCache = Cache.getInstance().getWeatherFromCache(key, CACHE_TIME_IN_HOURS);
        if(weatherFromCache != null) {
            return weatherFromCache;
        }
        try {
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
            weatherYr.properties().timeseries().forEach(t ->
                    weather.addTemperature(t.time(), (float) t.data().instant().details().air_temperature()));
            Cache.getInstance().put(key, weather);
            return weather;
        } catch (Exception e){
            e.printStackTrace();
            throw new ApiConnectionException("Could not connect to YR API, please contact the site administrator");
        }
    }
}
