package com.yeginamgim.place.service;

import com.yeginamgim.board.dto.PlaceInfo;
import com.yeginamgim.board.entity.BoardEntity;
import com.yeginamgim.board.repository.BoardRepository;
import com.yeginamgim.global.exception.PlaceNotFoundException;
import com.yeginamgim.place.dto.request.PlaceSearchRequest;
import com.yeginamgim.place.dto.response.PlaceResponse;
import com.yeginamgim.place.dto.response.PopularPlaceResponse;
import com.yeginamgim.place.repository.PlaceCsvStore;
import com.yeginamgim.place.util.GeoUtils;
import com.yeginamgim.place.util.PlaceSearchRequestValidator;
import com.yeginamgim.trace.repository.TraceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    public List<PlaceResponse> searchNearbyPlaces(PlaceSearchRequest request) {
        PlaceSearchRequest safeRequest = placeSearchRequestValidator.validateNearby(request);
        int limit = placeSearchRequestValidator.normalizeLimit(safeRequest.getLimit());
        int radius = placeSearchRequestValidator.normalizeRadius(safeRequest.getRadius());

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
        int normalizedLimit = placeSearchRequestValidator.normalizeLimit(limit);
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
        placeSearchRequestValidator.validateKakaoPlaceId(kakaoPlaceId);

        return placeCsvStore.findByKakaoPlaceId(kakaoPlaceId)
                .orElseThrow(PlaceNotFoundException::new);
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
                GeoUtils.distanceInMeters(latitude, longitude, left.getLatitude(), left.getLongitude()),
                GeoUtils.distanceInMeters(latitude, longitude, right.getLatitude(), right.getLongitude())
        );
    }

    private long defaultLong(Long value) {
        return value == null ? 0 : value;
    }
}
