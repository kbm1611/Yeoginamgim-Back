package com.yeginamgim.place.util;

import com.yeginamgim.global.exception.InvalidPlaceRequestException;
import com.yeginamgim.place.dto.request.PlaceSearchRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Set;

@Component
public class PlaceSearchRequestValidator {

    private static final int DEFAULT_LIMIT = 15; // 기본 리미트
    private static final int MAX_LIMIT = 15; // 최대 리미트
    private static final int DEFAULT_RADIUS = 1000;
    private static final int MAX_RADIUS = 20000;
    private static final Set<String> ALLOWED_NEARBY_CATEGORIES = Set.of("cafe", "food", "shop", "park", "culture");

    // 주변 장소 조회시 예외 검증
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

        String category = request.getCategory().trim().toLowerCase();
        // 기존 카테고리 목록에 없다면
        if ("all".equals(category) || !ALLOWED_NEARBY_CATEGORIES.contains(category)) {
            throw new InvalidPlaceRequestException("지원하지 않는 주변 장소 카테고리입니다.");
        }

        request.setCategory(category);
        return request;
    }

    // 카카오Id검증
    public void validateKakaoPlaceId(String kakaoPlaceId) {
        if (!StringUtils.hasText(kakaoPlaceId)) {
            throw new InvalidPlaceRequestException("kakaoPlaceId는 필수입니다.");
        }
    }

    // 호출 제한 일반화
    public int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    // 반경 일반화
    public int normalizeRadius(Integer radius) {
        return radius == null || radius <= 0 ? DEFAULT_RADIUS : radius;
    }

    // 좌표 위, 경도 검증
    private boolean isValidLatitude(Double latitude) {
        return latitude != null && !latitude.isNaN() && !latitude.isInfinite() && latitude >= -90 && latitude <= 90;
    }

    private boolean isValidLongitude(Double longitude) {
        return longitude != null && !longitude.isNaN() && !longitude.isInfinite() && longitude >= -180 && longitude <= 180;
    }
}
