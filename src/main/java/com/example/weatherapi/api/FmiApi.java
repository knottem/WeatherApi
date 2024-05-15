package com.example.weatherapi.api;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.domain.weather.WeatherFmi;
import com.example.weatherapi.exceptions.ApiConnectionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.example.weatherapi.util.DateUtils.generateFutureTimestamp;
import static com.example.weatherapi.util.WeatherMapper.createBaseWeather;

@Component
public class FmiApi {

    ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
    XmlMapper xmlMapper = new XmlMapper();
    private static final Logger LOG = LoggerFactory.getLogger(FmiApi.class);
    private boolean isTestMode = false;

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
        LOG.info("Fetching weather data from the FMI API...");
        WeatherFmi weatherFmi = fetchWeatherFMI(lon, lat, city);
        Weather weather = createBaseWeather(lon, lat, city);
        addWeatherDataFmi(weather, weatherFmi);
        return weather;
    }

    private WeatherFmi fetchWeatherFMI(double lon, double lat, City city) throws ApiConnectionException {
        try {
            String xmlContent;
            if (isTestMode) {
                String cityName = city.getName().toLowerCase();
                if(cityName.equals("rågsved")) cityName = "rågsvedexample-10days";
                LOG.info("Using test data for FMI: {}", cityName);
                xmlContent = new String(Files.readAllBytes(new File("src/test/resources/weatherexamples/fmi/" + cityName + ".xml").toPath()));
            } else {
                xmlContent = getXmlContentFromUrl(
                        getUrlFMI(lon, lat,
                                generateFutureTimestamp(ZonedDateTime.now(ZoneOffset.UTC), 10)));
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
            float temperature = temperatures.getOrDefault(validTime, Float.NaN);
            float windSpeed = windSpeeds.getOrDefault(validTime, Float.NaN);
            float windDirection = windDirections.getOrDefault(validTime, Float.NaN);
            float precipitation = precipitations.getOrDefault(validTime, Float.NaN);
            int weatherCode = -1;
            weather.addWeatherData(validTime, temperature, weatherCode, windSpeed, windDirection, precipitation);
        }
    }

    private String parseXml(String xmlContent) {
        xmlContent = xmlContent.replace("&param=", "&amp;param=").replace("&language=", "&amp;language=");
        return xmlContent;
    }

    private String getXmlContentFromUrl(URL url) throws IOException {
        try (InputStream inputStream = url.openStream()) {
            return new String(inputStream.readAllBytes());
        }
    }

    private String extractRelevantPart(String id) {
        if (id == null) {
            return "";
        }
        int lastDashIndex = id.lastIndexOf('-');
        return lastDashIndex != -1 ? id.substring(lastDashIndex + 1) : id;
    }

}