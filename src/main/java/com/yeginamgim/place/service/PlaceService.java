package com.yeginamgim.place.service;

import com.yeginamgim.board.dto.PlaceInfo;
import com.yeginamgim.board.entity.BoardEntity;
import com.yeginamgim.board.repository.BoardRepository;
import com.yeginamgim.global.exception.InvalidPlaceRequestException;
import com.yeginamgim.global.exception.PlaceNotFoundException;
import com.yeginamgim.place.dto.request.PlaceSearchRequest;
import com.yeginamgim.place.dto.response.PlaceResponse;
import com.yeginamgim.place.dto.response.PopularPlaceResponse;
import com.yeginamgim.trace.repository.TraceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 20;
    private static final int DEFAULT_RADIUS = 1000;
    private static final int MAX_RADIUS = 20000;
    private static final Set<String> ALLOWED_NEARBY_CATEGORIES = Set.of("cafe", "food", "shop", "park", "culture");

    private final KakaoLocalService kakaoLocalService;
    private final BoardRepository boardRepository;
    private final TraceRepository traceRepository;
    private final PlaceCsvStore placeCsvStore;

    // 주변 장소를 CSV 캐시에서 우선 조회하고 부족한 결과는 Kakao Local API로 보강한다.
    public List<PlaceResponse> searchNearbyPlaces(PlaceSearchRequest request) {
        PlaceSearchRequest safeRequest = validateNearbyRequest(request);
        int limit = normalizeLimit(safeRequest.getLimit());
        int radius = normalizeRadius(safeRequest.getRadius());

        List<PlaceInfo> cachedPlaces = placeCsvStore.findNearby(
                safeRequest.getLatitude(),
                safeRequest.getLongitude(),
                safeRequest.getCategory(),
                radius
        );

        List<PlaceResponse> cachedResponses = toPlaceResponses(cachedPlaces).stream()
                .sorted((left, right) -> compareCachedResponses(
                        left,
                        right,
                        safeRequest.getLatitude(),
                        safeRequest.getLongitude()
                ))
                .limit(limit)
                .toList();

        Map<String, PlaceResponse> responsesByKakaoPlaceId = new LinkedHashMap<>();
        cachedResponses.forEach(response -> responsesByKakaoPlaceId.put(response.getKakaoPlaceId(), response));

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
                    .forEach(response -> responsesByKakaoPlaceId.putIfAbsent(response.getKakaoPlaceId(), response));
        }

        return responsesByKakaoPlaceId.values().stream()
                .limit(limit)
                .toList();
    }

    // active 흔적 수가 많은 장소를 인기 장소 순위로 반환한다.
    public List<PopularPlaceResponse> getPopularPlaces(Integer limit) {
        int normalizedLimit = normalizeLimit(limit);
        AtomicInteger rank = new AtomicInteger(1);
        List<TraceRepository.PlaceTraceCount> traceCounts = traceRepository.countActiveTracesByPlace();
        Map<String, PlaceInfo> placeInfosByKakaoPlaceId = placeCsvStore.findAll().stream()
                .filter(placeInfo -> StringUtils.hasText(placeInfo.getKakaoPlaceId()))
                .collect(Collectors.toMap(
                        PlaceInfo::getKakaoPlaceId,
                        Function.identity(),
                        (left, right) -> left
                ));
        Map<String, Long> boardIdsByKakaoPlaceId = findBoardIds(traceCounts.stream()
                .map(TraceRepository.PlaceTraceCount::getKakaoPlaceId)
                .toList());

        return traceCounts.stream()
                .map(count -> {
                    PlaceInfo placeInfo = placeInfosByKakaoPlaceId.get(count.getKakaoPlaceId());
                    if (placeInfo == null) {
                        return null;
                    }

                    return PopularPlaceResponse.from(
                            rank.getAndIncrement(),
                            placeInfo,
                            count.getTraceCount(),
                            boardIdsByKakaoPlaceId.get(placeInfo.getKakaoPlaceId())
                    );
                })
                .filter(response -> response != null)
                .limit(normalizedLimit)
                .toList();
    }

    // kakaoPlaceId로 로컬 CSV 캐시에 저장된 장소 정보를 찾는다.
    public PlaceInfo findPlaceInfoByKakaoPlaceId(String kakaoPlaceId) {
        validateKakaoPlaceId(kakaoPlaceId);

        return placeCsvStore.findByKakaoPlaceId(kakaoPlaceId)
                .orElseThrow(PlaceNotFoundException::new);
    }

    // 장소 목록에 보드 ID와 흔적 수를 붙여 주변 장소 응답 목록으로 변환한다.
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

    // 단일 장소에 보드 ID와 흔적 수를 붙여 주변 장소 응답으로 변환한다.
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

    // 장소 ID 목록을 기준으로 active 흔적 수를 한 번에 집계한다.
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

    // 장소 ID 목록을 기준으로 연결된 보드 ID를 한 번에 조회한다.
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

    // 캐시 장소를 보드 존재 여부, 흔적 수, 거리 순으로 정렬하기 위한 비교값을 계산한다.
    private int compareCachedResponses(PlaceResponse left, PlaceResponse right, Double latitude, Double longitude) {
        int boardCompare = Boolean.compare(right.getBoardId() != null, left.getBoardId() != null);
        if (boardCompare != 0) {
            return boardCompare;
        }

        int traceCompare = Long.compare(defaultLong(right.getTraceCount()), defaultLong(left.getTraceCount()));
        if (traceCompare != 0) {
            return traceCompare;
        }

        return Double.compare(
                distanceInMeters(latitude, longitude, left.getLatitude(), left.getLongitude()),
                distanceInMeters(latitude, longitude, right.getLatitude(), right.getLongitude())
        );
    }

    // 주변 장소 조회 요청의 필수값과 허용 범위를 검증한다.
    private PlaceSearchRequest validateNearbyRequest(PlaceSearchRequest request) {
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
        if ("all".equals(category) || !ALLOWED_NEARBY_CATEGORIES.contains(category)) {
            throw new InvalidPlaceRequestException("지원하지 않는 주변 장소 카테고리입니다.");
        }

        request.setCategory(category);
        return request;
    }

    // 장소 단건 조회에 필요한 kakaoPlaceId가 비어 있지 않은지 검증한다.
    private void validateKakaoPlaceId(String kakaoPlaceId) {
        if (!StringUtils.hasText(kakaoPlaceId)) {
            throw new InvalidPlaceRequestException("kakaoPlaceId는 필수입니다.");
        }
    }

    // 요청 limit이 없거나 잘못된 경우 기본값을 쓰고 최대 응답 개수를 제한한다.
    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, MAX_LIMIT);
    }

    // 요청 반경이 없으면 기본 반경을 사용한다.
    private int normalizeRadius(Integer radius) {
        return radius == null || radius <= 0 ? DEFAULT_RADIUS : radius;
    }

    // 위도가 지리 좌표 범위 안의 유효한 숫자인지 확인한다.
    private boolean isValidLatitude(Double latitude) {
        return latitude != null && !latitude.isNaN() && !latitude.isInfinite() && latitude >= -90 && latitude <= 90;
    }

    // 경도가 지리 좌표 범위 안의 유효한 숫자인지 확인한다.
    private boolean isValidLongitude(Double longitude) {
        return longitude != null && !longitude.isNaN() && !longitude.isInfinite() && longitude >= -180 && longitude <= 180;
    }

    // null 집계값을 0으로 보정한다.
    private long defaultLong(Long value) {
        return value == null ? 0 : value;
    }

    // 두 위경도 좌표 사이의 거리를 미터 단위로 계산한다.
    private double distanceInMeters(Double latitude1, Double longitude1, Double latitude2, Double longitude2) {
        if (latitude1 == null || longitude1 == null || latitude2 == null || longitude2 == null) {
            return Double.MAX_VALUE;
        }

        double earthRadius = 6371000;
        double latDistance = Math.toRadians(latitude2 - latitude1);
        double lonDistance = Math.toRadians(longitude2 - longitude1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }
}
