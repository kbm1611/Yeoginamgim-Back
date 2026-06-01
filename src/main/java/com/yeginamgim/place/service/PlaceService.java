package com.yeginamgim.place.service;

import com.yeginamgim.board.dto.PlaceInfo;
import com.yeginamgim.board.entity.BoardEntity;
import com.yeginamgim.board.repository.BoardRepository;
import com.yeginamgim.place.dto.request.PlaceSearchRequest;
import com.yeginamgim.place.dto.response.PlaceResponse;
import com.yeginamgim.place.dto.response.PopularPlaceResponse;
import com.yeginamgim.trace.repository.TraceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int DEFAULT_RADIUS = 2000;
    private static final String PLACES_CSV = "places.csv";

    private final KakaoLocalService kakaoLocalService;
    private final BoardRepository boardRepository;
    private final TraceRepository traceRepository;

    public PlaceResponse getPlaceByKakaoPlaceId(String kakaoPlaceId) {
        validateKakaoPlaceId(kakaoPlaceId);

        PlaceInfo placeInfo = kakaoLocalService.findByKakaoPlaceId(kakaoPlaceId)
                .or(() -> findCsvPlaceByKakaoPlaceId(kakaoPlaceId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found."));

        return toPlaceResponse(placeInfo);
    }

    public List<PlaceResponse> searchNearbyPlaces(PlaceSearchRequest request) {
        PlaceSearchRequest safeRequest = request == null ? new PlaceSearchRequest() : request;

        List<PlaceInfo> kakaoPlaces = kakaoLocalService.searchByKeyword(safeRequest);
        List<PlaceInfo> places = kakaoPlaces.isEmpty()
                ? searchCsvPlaces(safeRequest)
                : kakaoPlaces;

        return places.stream()
                .limit(normalizeLimit(safeRequest.getLimit()))
                .map(this::toPlaceResponse)
                .toList();
    }

    public List<PopularPlaceResponse> getPopularPlaces(Integer limit) {
        int normalizedLimit = normalizeLimit(limit);
        AtomicInteger rank = new AtomicInteger(1);

        return traceRepository.countActiveTracesByPlace().stream()
                .limit(normalizedLimit)
                .map(count -> {
                    PlaceInfo placeInfo = kakaoLocalService.findByKakaoPlaceId(count.getKakaoPlaceId())
                            .or(() -> findCsvPlaceByKakaoPlaceId(count.getKakaoPlaceId()))
                            .orElse(null);

                    if (placeInfo == null) {
                        return null;
                    }

                    Long boardId = findBoardId(placeInfo.getKakaoPlaceId());
                    return PopularPlaceResponse.from(
                            rank.getAndIncrement(),
                            placeInfo,
                            count.getTraceCount(),
                            boardId
                    );
                })
                .filter(response -> response != null)
                .toList();
    }

    public PlaceInfo findPlaceInfoByKakaoPlaceId(String kakaoPlaceId) {
        validateKakaoPlaceId(kakaoPlaceId);

        return kakaoLocalService.findByKakaoPlaceId(kakaoPlaceId)
                .or(() -> findCsvPlaceByKakaoPlaceId(kakaoPlaceId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found."));
    }

    private PlaceResponse toPlaceResponse(PlaceInfo placeInfo) {
        return PlaceResponse.from(
                placeInfo,
                traceRepository.countActiveByKakaoPlaceId(placeInfo.getKakaoPlaceId()),
                findBoardId(placeInfo.getKakaoPlaceId())
        );
    }

    private Long findBoardId(String kakaoPlaceId) {
        return boardRepository.findByKakaoPlaceId(kakaoPlaceId)
                .map(BoardEntity::getBoardId)
                .orElse(null);
    }

    private Optional<PlaceInfo> findCsvPlaceByKakaoPlaceId(String kakaoPlaceId) {
        return loadCsvPlaces().stream()
                .filter(place -> kakaoPlaceId.equals(place.getKakaoPlaceId()))
                .findFirst();
    }

    private List<PlaceInfo> searchCsvPlaces(PlaceSearchRequest request) {
        String query = normalize(request.getQuery());
        String category = normalize(request.getCategory());
        int radius = request.getRadius() == null || request.getRadius() <= 0
                ? DEFAULT_RADIUS
                : request.getRadius();

        return loadCsvPlaces().stream()
                .filter(place -> matchesQuery(place, query))
                .filter(place -> matchesCategory(place, category))
                .filter(place -> isWithinRadius(place, request.getLatitude(), request.getLongitude(), radius))
                .sorted(Comparator.comparing(PlaceInfo::getPlaceName, Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    private boolean matchesQuery(PlaceInfo place, String query) {
        if (!StringUtils.hasText(query)) {
            return true;
        }

        return contains(place.getPlaceName(), query)
                || contains(place.getAddress(), query)
                || contains(place.getGroupName(), query);
    }

    private boolean matchesCategory(PlaceInfo place, String category) {
        if (!StringUtils.hasText(category) || "all".equals(category)) {
            return true;
        }

        return contains(place.getGroupName(), category);
    }

    private boolean isWithinRadius(PlaceInfo place, Double latitude, Double longitude, int radius) {
        if (latitude == null || longitude == null || place.getLatitude() == null || place.getLongitude() == null) {
            return true;
        }

        return distanceInMeters(latitude, longitude, place.getLatitude(), place.getLongitude()) <= radius;
    }

    private double distanceInMeters(double latitude1, double longitude1, double latitude2, double longitude2) {
        double earthRadius = 6371000;
        double latDistance = Math.toRadians(latitude2 - latitude1);
        double lonDistance = Math.toRadians(longitude2 - longitude1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    private boolean contains(String value, String keyword) {
        return StringUtils.hasText(value) && normalize(value).contains(keyword);
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase() : "";
    }

    private List<PlaceInfo> loadCsvPlaces() {
        ClassPathResource resource = new ClassPathResource(PLACES_CSV);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines()
                    .filter(line -> StringUtils.hasText(line) && !line.startsWith("#"))
                    .skip(1)
                    .map(this::toPlaceInfo)
                    .flatMap(Optional::stream)
                    .toList();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to read places.csv.");
        }
    }

    private Optional<PlaceInfo> toPlaceInfo(String line) {
        String[] columns = line.split(",", -1);
        if (columns.length < 8) {
            return Optional.empty();
        }

        return Optional.of(PlaceInfo.builder()
                .kakaoPlaceId(columns[0])
                .placeName(columns[1])
                .latitude(parseDouble(columns[2]))
                .longitude(parseDouble(columns[3]))
                .phone(columns[4])
                .address(columns[5])
                .kakaoMapUrl(columns[6])
                .groupName(columns[7])
                .build());
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

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, 50);
    }

    private void validateKakaoPlaceId(String kakaoPlaceId) {
        if (!StringUtils.hasText(kakaoPlaceId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "kakaoPlaceId is required.");
        }
    }
}
