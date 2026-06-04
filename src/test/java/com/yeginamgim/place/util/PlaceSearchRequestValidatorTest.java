package com.yeginamgim.place.util;

import com.yeginamgim.global.exception.InvalidPlaceRequestException;
import com.yeginamgim.place.dto.request.PlaceSearchRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlaceSearchRequestValidatorTest {

    private final PlaceSearchRequestValidator validator = new PlaceSearchRequestValidator();

    @ParameterizedTest
    @MethodSource("supportedKakaoCategoryCodes")
    void validateNearbyAcceptsOfficialKakaoCategoryCodes(String categoryCode) {
        PlaceSearchRequest request = PlaceSearchRequest.builder()
                .latitude(37.4979)
                .longitude(127.0276)
                .category(" " + categoryCode.toLowerCase() + " ")
                .build();

        PlaceSearchRequest validated = validator.validateNearby(request);

        assertThat(validated.getCategory()).isEqualTo(categoryCode);
    }

    @Test
    void validateNearbyAcceptsKoreanCategoryLabel() {
        PlaceSearchRequest request = PlaceSearchRequest.builder()
                .latitude(37.4979)
                .longitude(127.0276)
                .category(" \uD3B8\uC758\uC810 ")
                .build();

        PlaceSearchRequest validated = validator.validateNearby(request);

        assertThat(validated.getCategory()).isEqualTo("CS2");
    }

    @Test
    void validateNearbyRejectsRemovedShopCategory() {
        PlaceSearchRequest request = PlaceSearchRequest.builder()
                .latitude(37.4979)
                .longitude(127.0276)
                .category("shop")
                .build();

        assertThatThrownBy(() -> validator.validateNearby(request))
                .isInstanceOf(InvalidPlaceRequestException.class);
    }

    @ParameterizedTest
    @MethodSource("removedNearbyCategories")
    void validateNearbyRejectsRemovedKeywordCategories(String category) {
        PlaceSearchRequest request = PlaceSearchRequest.builder()
                .latitude(37.4979)
                .longitude(127.0276)
                .category(category)
                .build();

        assertThatThrownBy(() -> validator.validateNearby(request))
                .isInstanceOf(InvalidPlaceRequestException.class);
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
    void validateKeywordSearchTrimsQueryAndIgnoresCategoryAndLocationFilters() {
        PlaceSearchRequest request = PlaceSearchRequest.builder()
                .query("  coffee  ")
                .latitude(91.0)
                .longitude(181.0)
                .radius(20000)
                .category("not-a-search-filter")
                .build();

        PlaceSearchRequest validated = validator.validateKeywordSearch(request);

        assertThat(validated.getQuery()).isEqualTo("coffee");
        assertThat(validated.getCategory()).isNull();
        assertThat(validated.getLatitude()).isNull();
        assertThat(validated.getLongitude()).isNull();
        assertThat(validated.getRadius()).isNull();
    }

    @Test
    void validateKeywordSearchKeepsValidLocationWithDefaultKeywordRadius() {
        PlaceSearchRequest request = PlaceSearchRequest.builder()
                .query("  coffee  ")
                .latitude(37.5447)
                .longitude(127.0559)
                .category("not-a-search-filter")
                .build();

        PlaceSearchRequest validated = validator.validateKeywordSearch(request);

        assertThat(validated.getQuery()).isEqualTo("coffee");
        assertThat(validated.getCategory()).isNull();
        assertThat(validated.getLatitude()).isEqualTo(37.5447);
        assertThat(validated.getLongitude()).isEqualTo(127.0559);
        assertThat(validated.getRadius()).isEqualTo(2000);
    }

    @Test
    void validateKeywordSearchDoesNotRequireCoordinates() {
        PlaceSearchRequest request = PlaceSearchRequest.builder()
                .query("coffee")
                .build();

        PlaceSearchRequest validated = validator.validateKeywordSearch(request);

        assertThat(validated.getQuery()).isEqualTo("coffee");
    }

    @Test
    void normalizeLimitAndRadiusApplyDefaultsAndMaximums() {
        assertThat(validator.normalizeLimit(null)).isEqualTo(15);
        assertThat(validator.normalizeLimit(100)).isEqualTo(15);
        assertThat(validator.normalizeRadius(null)).isEqualTo(1000);
    }

    private static Stream<String> supportedKakaoCategoryCodes() {
        return Stream.of(
                "MT1",
                "CS2",
                "PS3",
                "SC4",
                "AC5",
                "PK6",
                "OL7",
                "SW8",
                "BK9",
                "CT1",
                "AG2",
                "PO3",
                "AT4",
                "AD5",
                "FD6",
                "CE7",
                "HP8",
                "PM9"
        );
    }

    private static Stream<String> removedNearbyCategories() {
        return Stream.of("library", "park");
    }
}
