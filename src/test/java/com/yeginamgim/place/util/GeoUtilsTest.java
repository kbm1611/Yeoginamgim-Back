package com.yeginamgim.place.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeoUtilsTest {

    @Test
    void distanceInMetersReturnsLargeValueWhenCoordinateIsMissing() {
        assertThat(GeoUtils.distanceInMeters(null, 127.0, 37.0, 127.0))
                .isEqualTo(Double.MAX_VALUE);
    }

    @Test
    void distanceInMetersCalculatesDistanceBetweenCoordinates() {
        double distance = GeoUtils.distanceInMeters(37.4979, 127.0276, 37.4980, 127.0277);

        assertThat(distance).isGreaterThan(0);
        assertThat(distance).isLessThan(20);
    }
}
