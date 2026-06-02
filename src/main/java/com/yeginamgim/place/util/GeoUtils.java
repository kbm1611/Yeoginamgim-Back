package com.yeginamgim.place.util;

public final class GeoUtils {

    private static final double EARTH_RADIUS_IN_METERS = 6371000;

    private GeoUtils() {
    }

    public static double distanceInMeters(Double latitude1, Double longitude1, Double latitude2, Double longitude2) {
        if (latitude1 == null || longitude1 == null || latitude2 == null || longitude2 == null) {
            return Double.MAX_VALUE;
        }

        double latDistance = Math.toRadians(latitude2 - latitude1);
        double lonDistance = Math.toRadians(longitude2 - longitude1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_IN_METERS * c;
    }
}
