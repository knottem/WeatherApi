package com.example.weatherapi.api;

import com.example.weatherapi.domain.weather.weatherFmi.*;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
class FmiApiTests {

        @Test
        void parseXmlTestToWeatherFmiTest4Hours() throws IOException {
            XmlMapper xmlMapper = new XmlMapper();
            File file = new File("src/test/resources/weatherexamples/fmi/rågsvedexample-4hours.xml");
            String xmlContent = new String(Files.readAllBytes(file.toPath()));
            xmlContent = xmlContent.replace("&param=", "&amp;param=").replace("&language=", "&amp;language=");
            FeatureCollection featureCollection = xmlMapper.readValue(xmlContent, FeatureCollection.class);
            assertThat(featureCollection).isNotNull();

            featureCollection.getMembers().stream()
                    .map(FeatureMember::getPointTimeSeriesObservation)
                    .filter(observation -> observation != null && observation.getResult() != null)
                    .map(PointTimeSeriesObservation::getResult)
                    .map(Result::getMeasurementTimeseries)
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
        FeatureCollection featureCollection = xmlMapper.readValue(xmlContent, FeatureCollection.class);
        assertThat(featureCollection).isNotNull();

        featureCollection.getMembers().stream()
                .map(FeatureMember::getPointTimeSeriesObservation)
                .filter(observation -> observation != null && observation.getResult() != null)
                .map(PointTimeSeriesObservation::getResult)
                .map(Result::getMeasurementTimeseries)
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

    private void assertMeasurement(MeasurementTimeseries timeseries, String expectedTime, double expectedValue) {
        Optional<MeasurementTVP> matchingPoint = timeseries.getPoints().stream()
                .map(MeasurementTimeseries.Point::getMeasurementTVP)
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
