package com.example.weatherapi.api;

import com.example.weatherapi.domain.City;

import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.domain.weather.WeatherFmi;
import com.example.weatherapi.exceptions.ApiConnectionException;
import com.example.weatherapi.services.WeatherApiService;
import com.example.weatherapi.util.HttpUtil;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import static com.example.weatherapi.util.DateUtils.generateFutureTimestamp;
import static com.example.weatherapi.util.WeatherMapper.createBaseWeather;

@Component
public class FmiApi {

    XmlMapper xmlMapper = new XmlMapper();
    private static final Logger LOG = LoggerFactory.getLogger(FmiApi.class);
    private final WeatherApiService weatherApiService;
    private boolean isTestMode = false;
    private final Object lock = new Object();

    @Autowired
    public FmiApi (WeatherApiService weatherApiService) {
        this.weatherApiService = weatherApiService;
    }

    public void setTestMode(boolean isTestMode) {
        this.isTestMode = isTestMode;
    }

    private URL getUrlFMI(double lon, double lat, String timestamp) throws IOException {
        //setting parameters to only get temperature and precipitation since the other parameters are not filled in by the FMI API
        return new URL("https://opendata.fmi.fi/wfs?service=WFS&version=2.0.0&request=getFeature&storedquery_id=ecmwf::forecast::surface::point::timevaluepair&latlon="
                + lat +
                ","
                + lon +
                "&endtime=" + timestamp +
                "&parameters=temperature,precipitation1h,humidity");
    }

    @Async
    public CompletableFuture<Weather> fetchWeatherFmiAsync(City city) {
        return CompletableFuture.completedFuture(getWeatherFMI(city.getLon(), city.getLat(), city));
    }

    public Weather getWeatherFMI(double lon, double lat, City city) {
        Weather weather = weatherApiService.fetchWeatherData("FMI", city, false, false, true);
        if(weather != null) {
            return weather;
        }
        synchronized (lock) {
            // Check again in case another thread has already fetched the data
            weather = weatherApiService.fetchWeatherDataCached("FMI", city);
            if(weather != null) {
                return weather;
            }
            LOG.info("Fetching weather data from the FMI API...");
            WeatherFmi weatherFmi = fetchWeatherFMI(lon, lat, city);
            weather = createBaseWeather(lon, lat, city, "FMI");
            addWeatherDataFmi(weather, weatherFmi);
            weatherApiService.saveWeatherData("FMI", weather, false, false, true);
            return weather;
        }
    }

    private WeatherFmi fetchWeatherFMI(double lon, double lat, City city) throws ApiConnectionException {
        try {
            String xmlContent;
            if (isTestMode) {
                String cityName = city.getName().toLowerCase();
                if (cityName.equals("rågsved")) cityName = "rågsvedexample-10days-2.xml";
                if (cityName.equals("stockholm")) cityName = "stockholmExample-2.xml";
                LOG.info("Using test data for FMI: {}", cityName);

                String resourcePath = "weatherexamples/fmi/" + cityName;
                try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                    if (inputStream == null) {
                        throw new IOException("Resource not found: " + resourcePath);
                    }
                    xmlContent = new Scanner(inputStream, StandardCharsets.UTF_8).useDelimiter("\\A").next();
                }
            } else {
                URI uri = getUrlFMI(lon, lat,
                        generateFutureTimestamp(ZonedDateTime.now(ZoneOffset.UTC), 9))
                        .toURI();
                HttpResponse<String> response = HttpUtil.getContentFromUrl(uri);

                if (response.statusCode() != 200) {
                    throw new ApiConnectionException("Error: Received status code " + response.statusCode());
                }

                xmlContent = response.body();
            }
            xmlContent = parseXml(xmlContent);
            return xmlMapper.readValue(xmlContent, WeatherFmi.class);
        } catch (Exception e) {
            LOG.error("Could not connect to FMI API");
            throw new ApiConnectionException("Could not connect to FMI API, please contact the site administrator");
        }
    }

    private void addWeatherDataFmi(Weather weather, WeatherFmi weatherFmi) {
        Map<ZonedDateTime, Float> temperatures = new HashMap<>();
        Map<ZonedDateTime, Float> windSpeeds = new HashMap<>();
        Map<ZonedDateTime, Float> windDirections = new HashMap<>();
        Map<ZonedDateTime, Float> precipitations = new HashMap<>();
        Map<ZonedDateTime, Float> humidities = new HashMap<>();

        for (WeatherFmi.FeatureMember member : weatherFmi.getMembers()) {
            WeatherFmi.PointTimeSeriesObservation observation = member.getPointTimeSeriesObservation();
            if (observation != null && observation.getResult() != null) {
                WeatherFmi.MeasurementTimeseries timeseries = observation.getResult().getMeasurementTimeseries();
                if (timeseries != null) {
                    String type = extractRelevantPart(timeseries.getId()).toLowerCase();
                    for (WeatherFmi.MeasurementTimeseries.Point point : timeseries.getPoints()) {
                        WeatherFmi.MeasurementTVP measurement = point.getMeasurementTVP();
                        if (measurement != null) {
                            ZonedDateTime validTime = ZonedDateTime.parse(measurement.getTime());
                            Double value = measurement.getValue();
                            if (value != null) {
                                switch (type) {
                                    case "temperature" -> temperatures.put(validTime, value.floatValue());
                                    case "windspeedms" -> windSpeeds.put(validTime, value.floatValue());
                                    case "winddirection" -> windDirections.put(validTime, value.floatValue());
                                    case "precipitation1h" -> precipitations.put(validTime, value.floatValue());
                                    case "humidity" -> humidities.put(validTime, value.floatValue());
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
            float humidity = sanitizeFloat(humidities.get(validTime));
            int weatherCode = -1;
            LOG.debug("Adding weather data for FMI: Time: {}, Temp: {}, Humidity: {}, Precipitation: {}", validTime, temperature, humidity, precipitation);
            weather.addWeatherData(validTime, temperature, weatherCode, windSpeed, windDirection, humidity, precipitation);
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