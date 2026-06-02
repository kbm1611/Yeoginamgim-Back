package com.yeginamgim.place.util;

import com.yeginamgim.global.exception.InvalidPlaceRequestException;
import com.yeginamgim.place.dto.request.PlaceSearchRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlaceSearchRequestValidatorTest {

    private final PlaceSearchRequestValidator validator = new PlaceSearchRequestValidator();

    @Test
    void validateNearbyNormalizesCategory() {
        PlaceSearchRequest request = PlaceSearchRequest.builder()
                .latitude(37.4979)
                .longitude(127.0276)
                .category(" CAFE ")
                .build();

        PlaceSearchRequest validated = validator.validateNearby(request);

        assertThat(validated.getCategory()).isEqualTo("cafe");
    }

    @Test
    void validateNearbyRejectsInvalidCoordinates() {
        PlaceSearchRequest request = PlaceSearchRequest.builder()
                .latitude(91.0)
                .longitude(127.0276)
                .category("cafe")
                .build();

        assertThatThrownBy(() -> validator.validateNearby(request))
                .isInstanceOf(InvalidPlaceRequestException.class);
    }

    @Test
    void normalizeLimitAndRadiusApplyDefaultsAndMaximums() {
        assertThat(validator.normalizeLimit(null)).isEqualTo(20);
        assertThat(validator.normalizeLimit(100)).isEqualTo(20);
        assertThat(validator.normalizeRadius(null)).isEqualTo(1000);
    }
}
