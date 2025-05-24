package com.example.weatherapi.util;

import com.example.weatherapi.domain.city.City;
import org.junit.jupiter.api.Test;

import static com.example.weatherapi.util.GeoUtils.haversine;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GeoUtilTests {

    private static final City STOCKHOLM = City.builder().lat(59.3293).lon(18.0686).build();
    private static final City GOTHENBURG = City.builder().lat(57.7089).lon(11.9746).build();
    private static final City MALMO = City.builder().lat(55.6050).lon(13.0038).build();
    private static final City UMEA = City.builder().lat(63.82580).lon(20.2630).build();

    @Test
    void testSameLocationReturnsZero() {
        double distance = distanceBetween(STOCKHOLM, STOCKHOLM);
        assertEquals(0.0, distance, 0.0001);
    }

    @Test
    void testSymmetry() {
        double aToB = distanceBetween(STOCKHOLM, GOTHENBURG);
        double bToA = distanceBetween(GOTHENBURG, STOCKHOLM);
        assertEquals(aToB, bToA, 0.0001);
    }

    @Test
    void testKnownDistanceStockholmToGothenburg() {
        double distance = distanceBetween(STOCKHOLM, GOTHENBURG);
        assertTrue(distance > 393 && distance < 397);
    }

    @Test
    void testKnownDistanceUmeaToMalmo() {
        double distance = distanceBetween(MALMO, UMEA);
        assertTrue(distance > 998 && distance < 1000);
    }

    @Test
    void testLatitudeOnlyDifference() {
        double distance = haversine(60.0, 15.0, 61.0, 15.0);
        assertTrue(distance > 110 && distance < 112);
    }

    @Test
    void testLongitudeOnlyDifference() {
        double equator = haversine(0.0, 0.0, 0.0, 1.0);
        double sweden = haversine(60.0, 15.0, 60.0, 16.0);
        assertTrue(equator > 110 && equator < 112);
        assertTrue(sweden > 55 && sweden < 56);
    }

    @Test
    void testSmallDistance() {
        double distance = haversine(60.0, 15.0, 60.001, 15.0);
        assertTrue(distance > 0.1 && distance < 0.2);
    }

    private double distanceBetween(City a, City b) {
        return haversine(a.getLat(), a.getLon(), b.getLat(), b.getLon());
    }
}
