package com.yeginamgim.place.util;

import com.yeginamgim.global.exception.InvalidPlaceRequestException;
import com.yeginamgim.place.dto.request.PlaceSearchRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PlaceSearchRequestValidator {

    private static final int DEFAULT_LIMIT = 15;
    private static final int MAX_LIMIT = 15;
    private static final int DEFAULT_RADIUS = 1000;
    private static final int DEFAULT_KEYWORD_RADIUS = 2000;
    private static final int MAX_RADIUS = 20000;

    public PlaceSearchRequest validateNearby(PlaceSearchRequest request) {
        if (request == null) {
            throw new InvalidPlaceRequestException("주변 장소 조회 요청이 필요합니다.");
        }

        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new InvalidPlaceRequestException("위도와 경도는 필수입니다.");
        }

        if (!isValidLatitude(request.getLatitude()) || !isValidLongitude(request.getLongitude())) {
            throw new InvalidPlaceRequestException("위도 또는 경도 범위가 올바르지 않습니다.");
        }

        if (request.getRadius() != null && (request.getRadius() <= 0 || request.getRadius() > MAX_RADIUS)) {
            throw new InvalidPlaceRequestException("반경은 1m 이상 20000m 이하여야 합니다.");
        }

        if (!StringUtils.hasText(request.getCategory())) {
            throw new InvalidPlaceRequestException("카테고리는 필수입니다.");
        }

        String categoryCode = PlaceCategory.toKakaoCategoryCode(request.getCategory()).orElse(null);
        if (!StringUtils.hasText(categoryCode)) {
            throw new InvalidPlaceRequestException("지원하지 않는 주변 장소 카테고리입니다.");
        }

        request.setCategory(categoryCode);
        return request;
    }

    public PlaceSearchRequest validateKeywordSearch(PlaceSearchRequest request) {
        if (request == null) {
            throw new InvalidPlaceRequestException("장소 검색 요청이 필요합니다.");
        }

        if (!StringUtils.hasText(request.getQuery())) {
            throw new InvalidPlaceRequestException("검색어는 필수입니다.");
        }

        request.setQuery(request.getQuery().trim());
        request.setCategory(null);

        if (isValidLatitude(request.getLatitude()) && isValidLongitude(request.getLongitude())) {
            request.setRadius(normalizeKeywordRadius(request.getRadius()));
            return request;
        }

        request.setLatitude(null);
        request.setLongitude(null);
        request.setRadius(null);

        return request;
    }

    public void validateKakaoPlaceId(String kakaoPlaceId) {
        if (!StringUtils.hasText(kakaoPlaceId)) {
            throw new InvalidPlaceRequestException("kakaoPlaceId는 필수입니다.");
        }
    }

    public int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    public int normalizeRadius(Integer radius) {
        if (radius == null || radius <= 0) {
            return DEFAULT_RADIUS;
        }

        return Math.min(radius, DEFAULT_RADIUS);
    }

    public int normalizeKeywordRadius(Integer radius) {
        if (radius == null || radius <= 0) {
            return DEFAULT_KEYWORD_RADIUS;
        }

        return Math.min(radius, MAX_RADIUS);
    }

    private boolean isValidLatitude(Double latitude) {
        return latitude != null && !latitude.isNaN() && !latitude.isInfinite() && latitude >= -90 && latitude <= 90;
    }

    private boolean isValidLongitude(Double longitude) {
        return longitude != null && !longitude.isNaN() && !longitude.isInfinite() && longitude >= -180 && longitude <= 180;
    }
}
