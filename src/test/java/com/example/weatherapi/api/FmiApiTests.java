package com.example.weatherapi.api;

import com.example.weatherapi.domain.City;
import com.example.weatherapi.domain.weather.Weather;
import com.example.weatherapi.domain.weather.WeatherFmi;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

import static com.example.weatherapi.utilitytests.WeatherTestUtils.assertWeatherDataFmiRagsved;
import static com.example.weatherapi.utilitytests.WeatherTestUtils.assertWeatherDataSmhiStockholm;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FmiApiTests {

    @Autowired
    private FmiApi fmiApi;

    @BeforeEach
    public void setupBeforeEach() {
        fmiApi.setTestMode(true);
    }

    @Test
    void getWeatherFmiTestRågsved_Valid() {
        City city = new City("Rågsved", 18.0319, 59.2572, null, null);
        Weather weather = fmiApi.getWeatherFMI(city.getLon(), city.getLat(), city);
        assertWeatherDataFmiRagsved(weather);
        System.out.println(weather.getWeatherData().get(ZonedDateTime.parse("2024-05-14T17:00:00Z")).toString());
    }

    @Test
    void parseXmlTestToWeatherFmiTest4Hours() throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        File file = new File("src/test/resources/weatherexamples/fmi/rågsvedexample-4hours.xml");
        String xmlContent = new String(Files.readAllBytes(file.toPath()));
        xmlContent = xmlContent.replace("&param=", "&amp;param=").replace("&language=", "&amp;language=");
        WeatherFmi weatherFmi = xmlMapper.readValue(xmlContent, WeatherFmi.class);
        assertThat(weatherFmi).isNotNull();

        weatherFmi.getMembers().stream()
                .map(WeatherFmi.FeatureMember::getPointTimeSeriesObservation)
                .filter(observation -> observation != null && observation.getResult() != null)
                .map(WeatherFmi.PointTimeSeriesObservation::getResult)
                .map(WeatherFmi.Result::getMeasurementTimeseries)
                .filter(Objects::nonNull)
                .forEach(timeseries -> {
                    switch (extractRelevantPart(timeseries.getId())) {
                        case "Temperature" -> {
                            assertMeasurement(timeseries, "2024-05-14T13:00:00Z", 20.2);
                            assertMeasurement(timeseries, "2024-05-14T14:00:00Z", 19.5);
                        }
                        case "WindSpeedMS", "WindDirection" -> {
                            assertMeasurement(timeseries, "2024-05-14T13:00:00Z", Double.NaN);
                            assertMeasurement(timeseries, "2024-05-14T14:00:00Z", Double.NaN);
                        }
                        case "Precipitation1h" -> {
                            assertMeasurement(timeseries, "2024-05-14T13:00:00Z", 0.0);
                            assertMeasurement(timeseries, "2024-05-14T14:00:00Z", 0.0);
                        }
                    }
                });
    }

    @Test
    void parseXmlTestToWeatherFmiTest10Days() throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        File file = new File("src/test/resources/weatherexamples/fmi/rågsvedexample-10Days.xml");
        String xmlContent = new String(Files.readAllBytes(file.toPath()));
        xmlContent = xmlContent.replace("&param=", "&amp;param=").replace("&language=", "&amp;language=");
        WeatherFmi weatherFmi = xmlMapper.readValue(xmlContent, WeatherFmi.class);
        assertThat(weatherFmi).isNotNull();

        weatherFmi.getMembers().stream()
                .map(WeatherFmi.FeatureMember::getPointTimeSeriesObservation)
                .filter(observation -> observation != null && observation.getResult() != null)
                .map(WeatherFmi.PointTimeSeriesObservation::getResult)
                .map(WeatherFmi.Result::getMeasurementTimeseries)
                .filter(Objects::nonNull)
                .forEach(timeseries -> {
                    switch (extractRelevantPart(timeseries.getId())) {
                        case "Temperature" -> {
                            assertMeasurement(timeseries, "2024-05-16T11:00:00Z", 20.0);
                            assertMeasurement(timeseries, "2024-05-16T12:00:00Z", 20.3);
                        }
                        case "WindSpeedMS", "WindDirection" -> {
                            assertMeasurement(timeseries, "2024-05-16T11:00:00Z", Double.NaN);
                            assertMeasurement(timeseries, "2024-05-16T12:00:00Z", Double.NaN);
                        }
                        case "Precipitation1h" -> {
                            assertMeasurement(timeseries, "2024-05-19T12:00:00Z", 0.0);
                            assertMeasurement(timeseries, "2024-05-19T13:00:00Z", 0.1);
                        }
                    }
                });
    }

    private void assertMeasurement(WeatherFmi.MeasurementTimeseries timeseries, String expectedTime, double expectedValue) {
        Optional<WeatherFmi.MeasurementTVP> matchingPoint = timeseries.getPoints().stream()
                .map(WeatherFmi.MeasurementTimeseries.Point::getMeasurementTVP)
                .filter(data -> expectedTime.equals(data.getTime()))
                .findFirst();

        assertThat(matchingPoint)
                .isPresent()
                .hasValueSatisfying(data -> {
                    if (Double.isNaN(expectedValue)) {
                        assertThat(data.getValue()).isNaN();
                    } else {
                        assertThat(data.getValue()).isEqualTo(expectedValue);
                    }
                });
    }

    private String extractRelevantPart(String id) {
        if (id == null) {
            return "";
        }
        int lastDashIndex = id.lastIndexOf('-');
        return lastDashIndex != -1 ? id.substring(lastDashIndex + 1) : id;
    }
}
