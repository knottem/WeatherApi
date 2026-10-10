package com.example.weatherapi.util;

/**
 * Utility methods for geographic calculations.
 *
 * @author Erik Wallenius
 * @see <a href="https://github.com/knottem/WeatherApi">Repository Link</a>
 */
public class GeoUtils {

    private GeoUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final int EARTH_RADIUS_KM = 6371;

    /**
     * Calculates the great-circle distance between two geographic coordinates
     * using the Haversine formula.
     *
     * @param lat1 latitude of the first coordinate in degrees
     * @param lon1 longitude of the first coordinate in degrees
     * @param lat2 latitude of the second coordinate in degrees
     * @param lon2 longitude of the second coordinate in degrees
     * @return the distance between the two coordinates in kilometers
     */
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}
