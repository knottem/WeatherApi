package com.example.weatherapi.api;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.domain.weather.WeatherSmhi;
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
import java.time.LocalDateTime;
import java.util.*;
//SmhiAPI class that handles all the communication with the smhi api
@Component
public class SmhiApi {

    ObjectMapper mapper;

    public SmhiApi() {
        this.mapper = JsonMapper.builder().findAndAddModules().build();
    }
    @Value("${cache.time.in.hours}")
    private int CACHE_TIME_IN_HOURS;

    // Method that creates the url for the smhi api
    private URL getUrlSmhi(double lon, double lat) throws IOException {
        return new URL("https://opendata-download-metfcst.smhi.se/api/category/pmp3g/version/2/geotype/point/lon/"
                + lon + "/lat/" + lat + "/data.json");
    }

    //The Smhi API doesn't need custom headers so this method is a lot simpler than the one in YrApi
    public Weather getWeatherSmhi(double lon, double lat, City cityObject) {
        String key = lon + "," + lat;
        //Checks if the weather is in the cache and if it is return it, else get the weather from the smhi api
        Weather weatherFromCache = Cache.getInstance().getWeatherFromCache(key, CACHE_TIME_IN_HOURS);
        if(weatherFromCache != null) {
            return weatherFromCache;
        }
        try {
            //Gets the weather from the smhi api
            WeatherSmhi weatherSmhi = mapper.readValue(getUrlSmhi(lon, lat), WeatherSmhi.class);
            if(weatherSmhi == null) {
                throw new ApiConnectionException("Could not connect to SMHI API, please contact the site administrator");
            }
            //Creates a new weather object and adds the city name to the message if the cityObject is not null
            Weather weather;
            if(cityObject == null){
                weather = Weather.builder()
                        .message("Weather for location Lon: " + lon + " and Lat: " + lat).build();
            } else {
                weather = Weather.builder()
                        .message("Weather for " + cityObject.getName() + " with location Lon: " + cityObject.getLon() + " and Lat: " + cityObject.getLat()).build();
            }

            //Adds the weather data to the weather object
            weatherSmhi.timeSeries().forEach(t -> {
                weather.addWeatherData(t.validTime(),
                       t.parameters().stream().filter(p -> p.name().equals("t")).map(p -> p.values().get(0)).findFirst().orElse(0f),
                        t.parameters().stream().filter(p -> p.name().equals("Wsymb2")).map(p -> p.values().get(0).intValue()).findFirst().orElse(0),
                        t.parameters().stream().filter(p -> p.name().equals("ws")).map(p -> p.values().get(0)).findFirst().orElse(0f),
                        t.parameters().stream().filter(p -> p.name().equals("wd")).map(p -> p.values().get(0)).findFirst().orElse(0f),
                        (t.parameters().stream().filter(p -> p.name().equals("pmin")).map(p -> p.values().get(0)).findFirst().orElse(0f)
                            + t.parameters().stream().filter(p -> p.name().equals("pmax")).map(p -> p.values().get(0)).findFirst().orElse(0f)) / 2);
            });
            //Adds the weather to the cache
            Cache.getInstance().put(key, weather);
            return weather;
        } catch (IOException e){
            e.printStackTrace();
            throw new ApiConnectionException("Could not connect to SMHI API, please contact the site administrator");
        }
    }

}
