package com.example.weatherapi.api;

import com.example.weatherapi.cache.CacheDB;
import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.domain.weather.WeatherFmi;
import com.example.weatherapi.exceptions.ApiConnectionException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import static com.example.weatherapi.util.DateUtils.generateFutureTimestamp;
import static com.example.weatherapi.util.HttpUtil.getContentFromUrl;
import static com.example.weatherapi.util.WeatherMapper.createBaseWeather;

@Component
public class FmiApi {

    XmlMapper xmlMapper = new XmlMapper();
    private static final Logger LOG = LoggerFactory.getLogger(FmiApi.class);
    private final CacheManager cacheManager;
    private final CacheDB cacheDB;
    private final String cacheName;
    private boolean isTestMode = false;

    @Autowired
    public FmiApi (CacheManager cacheManager, CacheDB cacheDB) {
        this.cacheManager = cacheManager;
        this.cacheDB = cacheDB;
        this.cacheName = "cache";
    }

    public void setTestMode(boolean isTestMode) {
        this.isTestMode = isTestMode;
    }

    private URL getUrlFMI(double lon, double lat, String timestamp) throws IOException {
        return new URL("https://opendata.fmi.fi/wfs?service=WFS&version=2.0.0&request=getFeature&storedquery_id=ecmwf::forecast::surface::point::timevaluepair&latlon=" + lat + "," + lon + "&endtime=" + timestamp);
    }

    @Async
    public CompletableFuture<Weather> fetchWeatherFmiAsync(City city) {
        return CompletableFuture.completedFuture(getWeatherFMI(city.getLon(), city.getLat(), city));
    }

    public Weather getWeatherFMI(double lon, double lat, City city) {
        String key = city.getName().toLowerCase() + "fmi";
        Weather weatherFromCache = Objects.requireNonNull(cacheManager.getCache(cacheName))
                .get(key, Weather.class);
        if(weatherFromCache != null) {
            LOG.info("Cache hit for City: {} in the cache, returning cached data for fmi", city.getName());
            return weatherFromCache;
        }

        Weather weatherFromCacheDB = cacheDB.getWeatherFromCache(city.getName(), false, false, true);
        if(weatherFromCacheDB != null) {
            Objects.requireNonNull(cacheManager.getCache(cacheName)).put(key, weatherFromCacheDB);
            return weatherFromCacheDB;
        }


        LOG.info("Fetching weather data from the FMI API...");
        WeatherFmi weatherFmi = fetchWeatherFMI(lon, lat, city);
        Weather weather = createBaseWeather(lon, lat, city, "FMI");
        addWeatherDataFmi(weather, weatherFmi);
        cacheDB.save(weather, false, false, true);
        Objects.requireNonNull(cacheManager.getCache(cacheName)).put(key, weather);
        return weather;
    }

    private WeatherFmi fetchWeatherFMI(double lon, double lat, City city) throws ApiConnectionException {
        try {
            String xmlContent;
            if (isTestMode) {
                String cityName = city.getName().toLowerCase();
                if (cityName.equals("rågsved")) cityName = "rågsvedexample-10days.xml";
                if (cityName.equals("stockholm")) cityName = "stockholmExample.xml";
                LOG.info("Using test data for FMI: {}", cityName);

                String resourcePath = "weatherexamples/fmi/" + cityName;
                try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                    if (inputStream == null) {
                        throw new IOException("Resource not found: " + resourcePath);
                    }
                    xmlContent = new Scanner(inputStream, StandardCharsets.UTF_8).useDelimiter("\\A").next();
                }
            } else {
                xmlContent = getContentFromUrl(
                        getUrlFMI(lon, lat,
                                generateFutureTimestamp(ZonedDateTime.now(ZoneOffset.UTC), 9)));
            }
            xmlContent = parseXml(xmlContent);
            return xmlMapper.readValue(xmlContent, WeatherFmi.class);
        } catch (IOException e) {
            LOG.error("Could not connect to FMI API");
            throw new ApiConnectionException("Could not connect to FMI API, please contact the site administrator");
        }
    }

    private void addWeatherDataFmi(Weather weather, WeatherFmi weatherFmi) {
        Map<ZonedDateTime, Float> temperatures = new HashMap<>();
        Map<ZonedDateTime, Float> windSpeeds = new HashMap<>();
        Map<ZonedDateTime, Float> windDirections = new HashMap<>();
        Map<ZonedDateTime, Float> precipitations = new HashMap<>();

        for (WeatherFmi.FeatureMember member : weatherFmi.getMembers()) {
            WeatherFmi.PointTimeSeriesObservation observation = member.getPointTimeSeriesObservation();
            if (observation != null && observation.getResult() != null) {
                WeatherFmi.MeasurementTimeseries timeseries = observation.getResult().getMeasurementTimeseries();
                if (timeseries != null) {
                    String type = extractRelevantPart(timeseries.getId());
                    for (WeatherFmi.MeasurementTimeseries.Point point : timeseries.getPoints()) {
                        WeatherFmi.MeasurementTVP measurement = point.getMeasurementTVP();
                        if (measurement != null) {
                            ZonedDateTime validTime = ZonedDateTime.parse(measurement.getTime());
                            Double value = measurement.getValue();
                            if (value != null) {
                                switch (type) {
                                    case "Temperature" -> temperatures.put(validTime, value.floatValue());
                                    case "WindSpeedMS" -> windSpeeds.put(validTime, value.floatValue());
                                    case "WindDirection" -> windDirections.put(validTime, value.floatValue());
                                    case "Precipitation1h" -> precipitations.put(validTime, value.floatValue());
                                }
                            }
                        }
                    }
                }
            }
        }

        for (ZonedDateTime validTime : temperatures.keySet()) {
            float temperature = sanitizeFloat(temperatures.get(validTime));
            float windSpeed = sanitizeFloat(windSpeeds.get(validTime));
            float windDirection = sanitizeFloat(windDirections.get(validTime));
            float precipitation = sanitizeFloat(precipitations.get(validTime));
            int weatherCode = -1;
            weather.addWeatherData(validTime, temperature, weatherCode, windSpeed, windDirection, precipitation);
        }
    }

    private float sanitizeFloat(Float value) {
        if (value == null || Float.isNaN(value)) {
            return -99;
        }
        return value;
    }

    private String parseXml(String xmlContent) {
        xmlContent = xmlContent.replace("&param=", "&amp;param=").replace("&language=", "&amp;language=");
        return xmlContent;
    }

    private String extractRelevantPart(String id) {
        if (id == null) {
            return "";
        }
        int lastDashIndex = id.lastIndexOf('-');
        return lastDashIndex != -1 ? id.substring(lastDashIndex + 1) : id;
    }

}