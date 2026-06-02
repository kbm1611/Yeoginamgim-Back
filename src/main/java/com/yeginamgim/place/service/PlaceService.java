package com.yeginamgim.place.service;

import com.yeginamgim.board.dto.PlaceInfo;
import com.yeginamgim.board.entity.BoardEntity;
import com.yeginamgim.board.repository.BoardRepository;
import com.yeginamgim.place.dto.request.PlaceSearchRequest;
import com.yeginamgim.place.dto.response.PlaceResponse;
import com.yeginamgim.place.dto.response.PopularPlaceResponse;
import com.yeginamgim.trace.repository.TraceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

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

    public PlaceInfo findPlaceInfoByKakaoPlaceId(String kakaoPlaceId) {
        validateKakaoPlaceId(kakaoPlaceId);

        return placeCsvStore.findByKakaoPlaceId(kakaoPlaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found in local cache."));
    }

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

    private PlaceSearchRequest validateNearbyRequest(PlaceSearchRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nearby place request is required.");
        }
        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "latitude and longitude are required.");
        }
        if (!isValidLatitude(request.getLatitude()) || !isValidLongitude(request.getLongitude())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "latitude or longitude is out of range.");
        }
        if (request.getRadius() != null && (request.getRadius() <= 0 || request.getRadius() > MAX_RADIUS)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "radius must be between 1 and 20000 meters.");
        }
        if (!StringUtils.hasText(request.getCategory())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "category is required.");
        }

        String category = request.getCategory().trim().toLowerCase();
        if ("all".equals(category) || !ALLOWED_NEARBY_CATEGORIES.contains(category)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported nearby place category.");
        }

        request.setCategory(category);
        return request;
    }

    private void validateKakaoPlaceId(String kakaoPlaceId) {
        if (!StringUtils.hasText(kakaoPlaceId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "kakaoPlaceId is required.");
        }
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, MAX_LIMIT);
    }

    private int normalizeRadius(Integer radius) {
        return radius == null || radius <= 0 ? DEFAULT_RADIUS : radius;
    }

    private boolean isValidLatitude(Double latitude) {
        return latitude != null && !latitude.isNaN() && !latitude.isInfinite() && latitude >= -90 && latitude <= 90;
    }

    private boolean isValidLongitude(Double longitude) {
        return longitude != null && !longitude.isNaN() && !longitude.isInfinite() && longitude >= -180 && longitude <= 180;
    }

    private long defaultLong(Long value) {
        return value == null ? 0 : value;
    }

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
