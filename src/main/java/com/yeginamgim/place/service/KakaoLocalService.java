package com.yeginamgim.place.service;

import com.yeginamgim.board.dto.PlaceInfo;
import com.yeginamgim.place.dto.request.PlaceSearchRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class KakaoLocalService {

    private static final String KAKAO_LOCAL_BASE_URL = "https://dapi.kakao.com";
    private static final int DEFAULT_RADIUS = 2000;
    private static final int DEFAULT_SIZE = 10;
    private static final Map<String, String> CATEGORY_CODES = Map.of(
            "cafe", "CE7",
            "food", "FD6",
            "culture", "CT1"
    );
    private static final Map<String, String> CATEGORY_KEYWORDS = Map.of(
            "park", "공원",
            "shop", "편집샵"
    );

    private final RestClient restClient;
    private final String restApiKey;

    public KakaoLocalService(@Value("${kakao.rest-api-key:}") String restApiKey) {
        this.restApiKey = restApiKey;
        this.restClient = RestClient.builder()
                .baseUrl(KAKAO_LOCAL_BASE_URL)
                .build();
    }

    public Optional<PlaceInfo> findByKakaoPlaceId(String kakaoPlaceId) {
        if (!StringUtils.hasText(kakaoPlaceId)) {
            return Optional.empty();
        }

        return searchByKeyword(PlaceSearchRequest.builder()
                .query(kakaoPlaceId)
                .limit(1)
                .build())
                .stream()
                .filter(place -> kakaoPlaceId.equals(place.getKakaoPlaceId()))
                .findFirst();
    }

    public List<PlaceInfo> searchByKeyword(PlaceSearchRequest request) {
        if (!hasApiKey() || request == null || !StringUtils.hasText(request.getQuery())) {
            return List.of();
        }

        try {
            KakaoKeywordResponse response = restClient.get()
                    .uri(uriBuilder -> keywordSearchUri(uriBuilder, request))
                    .header("Authorization", "KakaoAK " + restApiKey)
                    .retrieve()
                    .body(KakaoKeywordResponse.class);

            if (response == null || response.getDocuments() == null) {
                return List.of();
            }

            return response.getDocuments().stream()
                    .map(this::toPlaceInfo)
                    .toList();
        } catch (RuntimeException exception) {
            return List.of();
        }
    }

    public List<PlaceInfo> searchByCategory(PlaceSearchRequest request) {
        if (!hasApiKey() || request == null || !StringUtils.hasText(request.getCategory())) {
            return List.of();
        }

        String category = request.getCategory().trim().toLowerCase();
        String categoryCode = CATEGORY_CODES.get(category);
        if (StringUtils.hasText(categoryCode)) {
            return searchByKakaoCategoryCode(request, categoryCode);
        }

        String keyword = CATEGORY_KEYWORDS.get(category);
        if (StringUtils.hasText(keyword)) {
            return searchByKeyword(PlaceSearchRequest.builder()
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .radius(request.getRadius())
                    .query(keyword)
                    .page(request.getPage())
                    .limit(request.getLimit())
                    .build());
        }

        return List.of();
    }

    private List<PlaceInfo> searchByKakaoCategoryCode(PlaceSearchRequest request, String categoryCode) {
        if (request.getLatitude() == null || request.getLongitude() == null) {
            return List.of();
        }

        try {
            KakaoKeywordResponse response = restClient.get()
                    .uri(uriBuilder -> categorySearchUri(uriBuilder, request, categoryCode))
                    .header("Authorization", "KakaoAK " + restApiKey)
                    .retrieve()
                    .body(KakaoKeywordResponse.class);

            if (response == null || response.getDocuments() == null) {
                return List.of();
            }

            return response.getDocuments().stream()
                    .map(this::toPlaceInfo)
                    .toList();
        } catch (RuntimeException exception) {
            return List.of();
        }
    }

    private java.net.URI keywordSearchUri(UriBuilder uriBuilder, PlaceSearchRequest request) {
        UriBuilder builder = uriBuilder
                .path("/v2/local/search/keyword.json")
                .queryParam("query", request.getQuery())
                .queryParam("page", defaultPage(request.getPage()))
                .queryParam("size", defaultLimit(request.getLimit()));

        if (request.getLatitude() != null && request.getLongitude() != null) {
            builder.queryParam("y", request.getLatitude())
                    .queryParam("x", request.getLongitude())
                    .queryParam("radius", defaultRadius(request.getRadius()));
        }

        return builder.build();
    }

    private java.net.URI categorySearchUri(UriBuilder uriBuilder, PlaceSearchRequest request, String categoryCode) {
        return uriBuilder
                .path("/v2/local/search/category.json")
                .queryParam("category_group_code", categoryCode)
                .queryParam("y", request.getLatitude())
                .queryParam("x", request.getLongitude())
                .queryParam("radius", defaultRadius(request.getRadius()))
                .queryParam("page", defaultPage(request.getPage()))
                .queryParam("size", defaultLimit(request.getLimit()))
                .build();
    }

    private PlaceInfo toPlaceInfo(KakaoPlaceDocument document) {
        return PlaceInfo.builder()
                .kakaoPlaceId(document.getId())
                .placeName(document.getPlace_name())
                .latitude(parseDouble(document.getY()))
                .longitude(parseDouble(document.getX()))
                .phone(document.getPhone())
                .address(StringUtils.hasText(document.getRoad_address_name())
                        ? document.getRoad_address_name()
                        : document.getAddress_name())
                .kakaoMapUrl(document.getPlace_url())
                .groupName(document.getCategory_group_name())
                .build();
    }

    private boolean hasApiKey() {
        return StringUtils.hasText(restApiKey);
    }

    private int defaultRadius(Integer radius) {
        return radius == null || radius <= 0 ? DEFAULT_RADIUS : radius;
    }

    private int defaultPage(Integer page) {
        return page == null || page <= 0 ? 1 : page;
    }

    private int defaultLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(limit, 15);
    }

    private Double parseDouble(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    @Data
    private static class KakaoKeywordResponse {
        private List<KakaoPlaceDocument> documents;
    }

    @Data
    private static class KakaoPlaceDocument {
        private String id;
        private String place_name;
        private String category_group_name;
        private String phone;
        private String address_name;
        private String road_address_name;
        private String x;
        private String y;
        private String place_url;
    }
}
