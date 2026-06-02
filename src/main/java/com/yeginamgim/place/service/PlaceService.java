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
import org.springframework.transaction.annotation.Transactional;
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

    // 주변 장소 조회
    @Transactional(readOnly = true)
    public List<PlaceResponse> searchNearbyPlaces(PlaceSearchRequest request) {

        // 정상적인 요청인지 검증
        PlaceSearchRequest safeRequest = placeSearchRequestValidator.validateNearby(request);

        int limit = placeSearchRequestValidator.normalizeLimit(safeRequest.getLimit());
        int radius = placeSearchRequestValidator.normalizeRadius(safeRequest.getRadius());

        // 캐시에서 먼저 찾기
        List<PlaceInfo> cachedPlaces = placeCsvStore.findNearby(
                safeRequest.getLatitude(),
                safeRequest.getLongitude(),
                safeRequest.getCategory(),
                radius
        );

        // 캐시에 찾은 장소들은 응답 DTO로 바꾼 뒤 정렬 최대 limit 개수만
        List<PlaceResponse> cachedResponses = toPlaceResponses(cachedPlaces).stream()
                .sorted((left, right) -> compareCachedResponses(
                        left,
                        right,
                        safeRequest.getLatitude(),
                        safeRequest.getLongitude()
                ))
                .limit(limit)
                .toList();

        // 연결된 해시맵에 키를 카카오장소ID로 담기
        Map<String, PlaceResponse> responsesByKakaoPlaceId = new LinkedHashMap<>();
        cachedResponses.forEach(response -> responsesByKakaoPlaceId.put(response.getKakaoPlaceId(), response));

        // 15개보다 적을 경우 카카오API 호출
        if (responsesByKakaoPlaceId.size() < limit) {

            // 카카오API에 보낼 요청 만들기
            PlaceSearchRequest kakaoRequest = PlaceSearchRequest.builder()
                    .latitude(safeRequest.getLatitude())
                    .longitude(safeRequest.getLongitude())
                    .radius(radius)
                    .category(safeRequest.getCategory())
                    .limit(limit - responsesByKakaoPlaceId.size())
                    .page(safeRequest.getPage())
                    .build();

            // 응답을 map에 추가
            toPlaceResponses(kakaoLocalService.searchByCategory(kakaoRequest)).stream()
                    .forEach(response -> responsesByKakaoPlaceId.putIfAbsent(response.getKakaoPlaceId(), response));
        }

        // 리스트로 반환
        return responsesByKakaoPlaceId.values().stream()
                .limit(limit)
                .toList();
    }

    // 인기 장소 목록 조회
    @Transactional(readOnly = true)
    public List<PopularPlaceResponse> getPopularPlaces(Integer limit) {
        int normalizedLimit = placeSearchRequestValidator.normalizeLimit(limit);
        // 인기 순위를 붙이 위한 카운터. 람다식 안에서 쓰기 위한 객체
        AtomicInteger rank = new AtomicInteger(1);

        // 활성화된 흔적 개수 DB에서 가져오기
        List<TraceRepository.PlaceTraceCount> traceCounts = traceRepository.countActiveTracesByPlace();
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
                    if (placeInfo == null) {
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

    // 캐시에서 가져온 장소들을 어떤 순서로 보여줄지 정하는 비교 함수
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

    // Long값이 null이면 0으로 바꿔주는 메소드
    private long defaultLong(Long value) {
        return value == null ? 0 : value;
    }
}
