package com.yeginamgim.place.service;

import com.yeginamgim.board.dto.PlaceInfo;
import com.yeginamgim.board.entity.BoardEntity;
import com.yeginamgim.board.repository.BoardRepository;
import com.yeginamgim.global.exception.InvalidPlaceRequestException;
import com.yeginamgim.global.exception.PlaceNotFoundException;
import com.yeginamgim.global.util.PeriodRange;
import com.yeginamgim.place.dto.request.PlaceSearchRequest;
import com.yeginamgim.place.dto.response.PlaceResponse;
import com.yeginamgim.place.dto.response.PopularPlaceResponse;
import com.yeginamgim.place.repository.PlaceCsvStore;
import com.yeginamgim.place.util.GeoUtils;
import com.yeginamgim.place.util.PlaceCategory;
import com.yeginamgim.place.util.PlaceSearchRequestValidator;
import com.yeginamgim.trace.repository.TraceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final KakaoLocalService kakaoLocalService;
    private final BoardRepository boardRepository;
    private final TraceRepository traceRepository;
    private final PlaceCsvStore placeCsvStore;
    private final PlaceSearchRequestValidator placeSearchRequestValidator;

    // 주변 장소 조회
    @Transactional(readOnly = true)
    public List<PlaceResponse> searchNearbyPlaces(PlaceSearchRequest request) {

        // 정상적인 요청인지 검증
        PlaceSearchRequest safeRequest = placeSearchRequestValidator.validateNearby(request);

        int limit = placeSearchRequestValidator.normalizeLimit(safeRequest.getLimit());
        int radius = placeSearchRequestValidator.normalizeRadius(safeRequest.getRadius());

        List<PlaceInfo> cachedPlaces = placeCsvStore.findNearby(
                safeRequest.getLatitude(),
                safeRequest.getLongitude(),
                safeRequest.getCategory(),
                radius
        );

        Map<String, PlaceResponse> responsesByKakaoPlaceId = new LinkedHashMap<>();
        toPlaceResponses(cachedPlaces).stream()
                .sorted((left, right) -> compareNearbyPlaces(
                        left,
                        right,
                        safeRequest.getCategory(),
                        safeRequest.getLatitude(),
                        safeRequest.getLongitude()
                ))
                .limit(limit)
                .forEach(response -> putOrMergeResponse(responsesByKakaoPlaceId, response));

        if (responsesByKakaoPlaceId.size() < limit) {
            PlaceSearchRequest kakaoRequest = PlaceSearchRequest.builder()
                    .latitude(safeRequest.getLatitude())
                    .longitude(safeRequest.getLongitude())
                    .radius(radius)
                    .category(safeRequest.getCategory())
                    .limit(limit - responsesByKakaoPlaceId.size())
                    .page(safeRequest.getPage())
                    .build();

            toPlaceResponses(kakaoLocalService.searchByCategory(kakaoRequest)).stream()
                    .forEach(response -> putOrMergeResponse(responsesByKakaoPlaceId, response));
        }

        // 리스트로 반환
        return responsesByKakaoPlaceId.values().stream()
                .sorted((left, right) -> compareNearbyPlaces(
                        left,
                        right,
                        safeRequest.getCategory(),
                        safeRequest.getLatitude(),
                        safeRequest.getLongitude()
                ))
                .limit(limit)
                .toList();
    }

    // 인기 장소 목록 조회
    @Transactional(readOnly = true)
    public List<PlaceResponse> searchPlacesByKeyword(PlaceSearchRequest request) {
        PlaceSearchRequest safeRequest = placeSearchRequestValidator.validateKeywordSearch(request);

        int limit = placeSearchRequestValidator.normalizeLimit(safeRequest.getLimit());

        Map<String, PlaceInfo> placesByKakaoPlaceId = new LinkedHashMap<>();
        if (hasKeywordLocation(safeRequest)) {
            PlaceSearchRequest nearbyKakaoRequest = PlaceSearchRequest.builder()
                    .query(safeRequest.getQuery())
                    .latitude(safeRequest.getLatitude())
                    .longitude(safeRequest.getLongitude())
                    .radius(safeRequest.getRadius())
                    .limit(limit)
                    .page(safeRequest.getPage())
                    .build();

            putPlacesByKakaoPlaceId(
                    placesByKakaoPlaceId,
                    kakaoLocalService.searchByKeyword(nearbyKakaoRequest)
            );
        }

        PlaceSearchRequest globalKakaoRequest = PlaceSearchRequest.builder()
                .query(safeRequest.getQuery())
                .limit(limit)
                .page(safeRequest.getPage())
                .build();

        putPlacesByKakaoPlaceId(
                placesByKakaoPlaceId,
                kakaoLocalService.searchByKeyword(globalKakaoRequest)
        );

        return toPlaceResponses(placesByKakaoPlaceId.values().stream()
                .limit(limit)
                .toList());
    }

    @Transactional(readOnly = true)
    public List<PopularPlaceResponse> getPopularPlaces(Integer limit) {
        return getPopularPlaces(limit, null, null, null, null);
    }

    // 인기 장소 목록 조회
    @Transactional(readOnly = true)
    public List<PopularPlaceResponse> getPopularPlaces(Integer limit, String district) {
        return getPopularPlaces(limit, district, null, null, null);
    }

    // 인기 장소 목록 조회
    @Transactional(readOnly = true)
    public List<PopularPlaceResponse> getPopularPlaces(
            Integer limit,
            String district,
            Double latitude,
            Double longitude,
            Integer radius
    ) {
        return getPopularPlaces(limit, district, latitude, longitude, radius, null, false);
    }

    @Transactional(readOnly = true)
    public List<PopularPlaceResponse> getPopularPlaces(
            Integer limit,
            String district,
            Double latitude,
            Double longitude,
            Integer radius,
            String period
    ) {
        return getPopularPlaces(limit, district, latitude, longitude, radius, PeriodRange.startAt(period), true);
    }

    private List<PopularPlaceResponse> getPopularPlaces(
            Integer limit,
            String district,
            Double latitude,
            Double longitude,
            Integer radius,
            LocalDateTime startAt,
            boolean periodScoped
    ) {
        int normalizedLimit = placeSearchRequestValidator.normalizeLimit(limit);
        boolean locationFiltered = hasLocationFilter(latitude, longitude);
        int normalizedRadius = placeSearchRequestValidator.normalizeRadius(radius);
        // 인기 순위를 붙이 위한 카운터. 람다식 안에서 쓰기 위한 객체
        AtomicInteger rank = new AtomicInteger(1);
        String normalizedDistrict = normalizeDistrict(district);

        // 활성화된 흔적 개수 DB에서 가져오기
        List<TraceRepository.PlaceTraceCount> traceCounts = periodScoped
                ? traceRepository.countActiveTracesByPlaceSince(startAt)
                : traceRepository.countActiveTracesByPlace();
        // 카카오Id 별 장소 정보(캐시csv파일에서 가져옴)
        Map<String, PlaceInfo> placeInfosByKakaoPlaceId = placeCsvStore.findAll().stream()
                .filter(placeInfo -> StringUtils.hasText(placeInfo.getKakaoPlaceId()))
                .collect(Collectors.toMap(
                        PlaceInfo::getKakaoPlaceId,
                        Function.identity(), // 객체 자기 자신을 value로 쓰겠다라는 의미
                        (left, right) -> left
                ));
        // 인기 장소들의 kakaoId를 기준으로 이미 생성된 보드 ID를 한 번에 조회
        Map<String, Long> boardIdsByKakaoPlaceId = findBoardIds(traceCounts.stream()
                .map(TraceRepository.PlaceTraceCount::getKakaoPlaceId)
                .toList());

        // 장소별 trace 개수 목록을 돌면서 응답 DTO로 변환
        return traceCounts.stream()
                .map(count -> {
                    PlaceInfo placeInfo = placeInfosByKakaoPlaceId.get(count.getKakaoPlaceId());
                    if (placeInfo == null
                            || !matchesDistrict(placeInfo, normalizedDistrict)
                            || !matchesLocation(placeInfo, latitude, longitude, normalizedRadius, locationFiltered)) {
                        return null;
                    }

                    return PopularPlaceResponse.from(
                            rank.getAndIncrement(), // 랭크 1증가
                            placeInfo,
                            count.getTraceCount(),
                            boardIdsByKakaoPlaceId.get(placeInfo.getKakaoPlaceId())
                    );
                })
                .filter(response -> response != null)
                .limit(normalizedLimit)
                .toList();
    }

    private boolean hasLocationFilter(Double latitude, Double longitude) {
        if (latitude == null && longitude == null) {
            return false;
        }

        if (!isValidLatitude(latitude) || !isValidLongitude(longitude)) {
            throw new InvalidPlaceRequestException("위도와 경도 범위가 올바르지 않습니다.");
        }

        return true;
    }

    private boolean hasKeywordLocation(PlaceSearchRequest request) {
        return request.getLatitude() != null && request.getLongitude() != null;
    }

    private void putPlacesByKakaoPlaceId(Map<String, PlaceInfo> placesByKakaoPlaceId, List<PlaceInfo> placeInfos) {
        if (placeInfos == null) {
            return;
        }

        placeInfos.stream()
                .filter(placeInfo -> placeInfo != null && StringUtils.hasText(placeInfo.getKakaoPlaceId()))
                .forEach(placeInfo -> placesByKakaoPlaceId.merge(
                        placeInfo.getKakaoPlaceId(),
                        placeInfo,
                        this::mergePlaceInfo
                ));
    }

    private void putOrMergeResponse(Map<String, PlaceResponse> responsesByKakaoPlaceId, PlaceResponse response) {
        if (response == null || !StringUtils.hasText(response.getKakaoPlaceId())) {
            return;
        }

        responsesByKakaoPlaceId.merge(response.getKakaoPlaceId(), response, this::mergePlaceResponse);
    }

    private PlaceInfo mergePlaceInfo(PlaceInfo existing, PlaceInfo incoming) {
        return PlaceInfo.builder()
                .kakaoPlaceId(firstText(existing.getKakaoPlaceId(), incoming.getKakaoPlaceId()))
                .placeName(firstText(existing.getPlaceName(), incoming.getPlaceName()))
                .latitude(existing.getLatitude() != null ? existing.getLatitude() : incoming.getLatitude())
                .longitude(existing.getLongitude() != null ? existing.getLongitude() : incoming.getLongitude())
                .phone(firstText(existing.getPhone(), incoming.getPhone()))
                .address(firstText(existing.getAddress(), incoming.getAddress()))
                .kakaoMapUrl(firstText(existing.getKakaoMapUrl(), incoming.getKakaoMapUrl()))
                .groupName(firstText(existing.getGroupName(), incoming.getGroupName()))
                .build();
    }

    private PlaceResponse mergePlaceResponse(PlaceResponse existing, PlaceResponse incoming) {
        return PlaceResponse.builder()
                .kakaoPlaceId(firstText(existing.getKakaoPlaceId(), incoming.getKakaoPlaceId()))
                .placeName(firstText(existing.getPlaceName(), incoming.getPlaceName()))
                .latitude(existing.getLatitude() != null ? existing.getLatitude() : incoming.getLatitude())
                .longitude(existing.getLongitude() != null ? existing.getLongitude() : incoming.getLongitude())
                .phone(firstText(existing.getPhone(), incoming.getPhone()))
                .address(firstText(existing.getAddress(), incoming.getAddress()))
                .kakaoMapUrl(firstText(existing.getKakaoMapUrl(), incoming.getKakaoMapUrl()))
                .groupName(firstText(existing.getGroupName(), incoming.getGroupName()))
                .traceCount(Math.max(defaultLong(existing.getTraceCount()), defaultLong(incoming.getTraceCount())))
                .boardId(existing.getBoardId() != null ? existing.getBoardId() : incoming.getBoardId())
                .build();
    }

    private String firstText(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred : fallback;
    }

    private boolean matchesLocation(
            PlaceInfo placeInfo,
            Double latitude,
            Double longitude,
            int radius,
            boolean locationFiltered
    ) {
        if (!locationFiltered) {
            return true;
        }

        return GeoUtils.distanceInMeters(
                latitude,
                longitude,
                placeInfo.getLatitude(),
                placeInfo.getLongitude()
        ) <= radius;
    }

    private boolean isValidLatitude(Double latitude) {
        return latitude != null && !latitude.isNaN() && !latitude.isInfinite() && latitude >= -90 && latitude <= 90;
    }

    private boolean isValidLongitude(Double longitude) {
        return longitude != null && !longitude.isNaN() && !longitude.isInfinite() && longitude >= -180 && longitude <= 180;
    }

    private boolean matchesDistrict(PlaceInfo placeInfo, String district) {
        if (!StringUtils.hasText(district)) {
            return true;
        }
        return StringUtils.hasText(placeInfo.getAddress())
                && placeInfo.getAddress().contains(district);
    }

    private String normalizeDistrict(String district) {
        return StringUtils.hasText(district) ? district.trim() : "";
    }

    // kakaoPlaceID로 CSV 캐시에서 장소 하나를 찾는 메소드
    @Transactional(readOnly = true)
    public PlaceInfo findPlaceInfoByKakaoPlaceId(String kakaoPlaceId) {
        placeSearchRequestValidator.validateKakaoPlaceId(kakaoPlaceId);

        return placeCsvStore.findByKakaoPlaceId(kakaoPlaceId)
                .orElseThrow(PlaceNotFoundException::new);
    }

    // 장소 정보를 화면/API 응답용으로 바꾸면서 흔적 개수와 보드ID를 붙여주는 보조 메소드들
    private List<PlaceResponse> toPlaceResponses(List<PlaceInfo> placeInfos) {
        Map<String, Long> traceCountsByKakaoPlaceId = findTraceCounts(placeInfos);
        Map<String, Long> boardIdsByKakaoPlaceId = findBoardIds(placeInfos.stream()
                .map(PlaceInfo::getKakaoPlaceId)
                .toList());

        return placeInfos.stream()
                .map(placeInfo -> toPlaceResponse(
                        placeInfo,
                        traceCountsByKakaoPlaceId,
                        boardIdsByKakaoPlaceId
                ))
                .toList();
    }

    // 장소 하나를 응답 하나로
    private PlaceResponse toPlaceResponse(
            PlaceInfo placeInfo,
            Map<String, Long> traceCountsByKakaoPlaceId,
            Map<String, Long> boardIdsByKakaoPlaceId
    ) {
        return PlaceResponse.from(
                placeInfo,
                traceCountsByKakaoPlaceId.getOrDefault(placeInfo.getKakaoPlaceId(), 0L),
                boardIdsByKakaoPlaceId.get(placeInfo.getKakaoPlaceId())
        );
    }

    // 장소 목록을 받아서, 각 장소의 흔적 개수를 Map으로 반환
    private Map<String, Long> findTraceCounts(List<PlaceInfo> placeInfos) {
        List<String> kakaoPlaceIds = placeInfos.stream()
                .map(PlaceInfo::getKakaoPlaceId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (kakaoPlaceIds.isEmpty()) {
            return Map.of();
        }

        return traceRepository.countActiveByKakaoPlaceIds(kakaoPlaceIds).stream()
                .collect(Collectors.toMap(
                        TraceRepository.PlaceTraceCount::getKakaoPlaceId,
                        count -> defaultLong(count.getTraceCount()),
                        (left, right) -> left
                ));
    }

    // kakaoPlaceId 목록을 받아서, 각 장소의 보드 ID를 Map으로 반환
    private Map<String, Long> findBoardIds(List<String> kakaoPlaceIds) {
        List<String> uniqueKakaoPlaceIds = kakaoPlaceIds.stream()
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (uniqueKakaoPlaceIds.isEmpty()) {
            return Map.of();
        }

        return boardRepository.findByKakaoPlaceIdIn(uniqueKakaoPlaceIds).stream()
                .collect(Collectors.toMap(
                        BoardEntity::getKakaoPlaceId,
                        BoardEntity::getBoardId,
                        (left, right) -> left
                ));
    }

    // 선택 카테고리에 맞는 결과를 먼저 두고, 거리와 보드/trace 신호를 함께 본다.
    private int compareNearbyPlaces(
            PlaceResponse left,
            PlaceResponse right,
            String selectedCategory,
            Double latitude,
            Double longitude
    ) {
        int categoryCompare = Boolean.compare(
                matchesSelectedCategory(right, selectedCategory),
                matchesSelectedCategory(left, selectedCategory)
        );
        if (categoryCompare != 0) {
            return categoryCompare;
        }

        int distanceCompare = Double.compare(
                GeoUtils.distanceInMeters(latitude, longitude, left.getLatitude(), left.getLongitude()),
                GeoUtils.distanceInMeters(latitude, longitude, right.getLatitude(), right.getLongitude())
        );
        if (distanceCompare != 0) {
            return distanceCompare;
        }

        int boardCompare = Boolean.compare(right.getBoardId() != null, left.getBoardId() != null);
        if (boardCompare != 0) {
            return boardCompare;
        }

        int traceCompare = Long.compare(defaultLong(right.getTraceCount()), defaultLong(left.getTraceCount()));
        if (traceCompare != 0) {
            return traceCompare;
        }

        return defaultString(left.getPlaceName()).compareTo(defaultString(right.getPlaceName()));
    }

    private boolean matchesSelectedCategory(PlaceResponse response, String selectedCategory) {
        return PlaceCategory.matchesSelectedCategory(response.getPlaceName(), response.getGroupName(), selectedCategory);
    }

    // Long값이 null이면 0으로 바꿔주는 메소드
    private long defaultLong(Long value) {
        return value == null ? 0 : value;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
