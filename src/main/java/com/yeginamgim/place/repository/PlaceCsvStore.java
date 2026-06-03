package com.yeginamgim.place.repository;

import com.yeginamgim.board.dto.PlaceInfo;
import com.yeginamgim.place.util.GeoUtils;
import com.yeginamgim.place.util.PlaceCategory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class PlaceCsvStore {

    private static final String HEADER = "kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name";
    private final Path cacheFilePath;

    // 설정된 CSV 캐시 파일 경로를 초기화하고 파일이 없으면 생성한다.
    public PlaceCsvStore(@Value("${place.cache-file-path:../data/places-cache.csv}") String cacheFilePath) {
        this.cacheFilePath = Path.of(cacheFilePath).toAbsolutePath().normalize();
        ensureCacheFile();
    }

    // kakaoPlaceId와 일치하는 장소를 CSV 캐시에서 찾는다.
    public Optional<PlaceInfo> findByKakaoPlaceId(String kakaoPlaceId) {
        if (!StringUtils.hasText(kakaoPlaceId)) {
            return Optional.empty();
        }

        return findAll().stream()
                .filter(place -> kakaoPlaceId.equals(place.getKakaoPlaceId()))
                .findFirst(); // 첫번째로 찾은 값만 반환
    }

    // 지정 좌표 주변에서 카테고리와 반경 조건에 맞는 장소를 거리순으로 찾는다.
    public List<PlaceInfo> findNearby(Double latitude, Double longitude, String category, int radius) {
        if (latitude == null || longitude == null || !StringUtils.hasText(category)) {
            return List.of();
        }

        return findAll().stream()
                .filter(place -> matchesCategory(place, category))
                .filter(place -> isWithinRadius(latitude, longitude, place, radius))
                .sorted(Comparator.comparingDouble(place -> distanceFrom(latitude, longitude, place)))
                .toList();
    }

    // 같은 장소가 없으면 추가하고, 이미 있으면 누락된 정보를 병합해 저장한다.
    public synchronized void saveIfAbsent(PlaceInfo placeInfo) {
        if (placeInfo == null || !hasMinimumIdentity(placeInfo)) {
            return;
        }

        List<PlaceInfo> places = new ArrayList<>(findAll());
        Optional<PlaceInfo> existing = places.stream()
                .filter(place -> isSamePlace(place, placeInfo))
                .findFirst();
        if (existing.isPresent()) {
            int index = places.indexOf(existing.get());
            places.set(index, merge(existing.get(), placeInfo));
            writeAll(places);
            return;
        }

        places.add(placeInfo);
        writeAll(places);
    }

    // CSV 캐시에 저장된 모든 장소를 읽어온다.
    public List<PlaceInfo> findAll() {
        ensureCacheFile();

        try (BufferedReader reader = Files.newBufferedReader(cacheFilePath, StandardCharsets.UTF_8)) {
            return reader.lines()
                    .filter(line -> StringUtils.hasText(line) && !line.startsWith("#"))
                    .skip(1)
                    .map(this::toPlaceInfo)
                    .flatMap(Optional::stream)
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read place cache CSV.", exception);
        }
    }

    // 전체 장소 목록을 CSV 파일에 원자적으로 다시 쓴다.
    private void writeAll(List<PlaceInfo> places) {
        ensureCacheFile();
        Path tempPath = cacheFilePath.resolveSibling(cacheFilePath.getFileName() + ".tmp");
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        places.stream()
                .sorted(Comparator.comparing(place -> defaultString(place.getKakaoPlaceId())))
                .map(this::toCsvLine)
                .forEach(lines::add);

        try {
            Files.write(tempPath, lines, StandardCharsets.UTF_8);
            Files.move(tempPath, cacheFilePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write place cache CSV.", exception);
        }
    }

    // CSV 캐시 파일과 상위 디렉터리가 존재하도록 보장한다.
    private void ensureCacheFile() {
        try {
            Path parent = cacheFilePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(cacheFilePath)) {
                Files.writeString(cacheFilePath, HEADER + System.lineSeparator(), StandardCharsets.UTF_8);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to initialize place cache CSV.", exception);
        }
    }

    // CSV 한 줄을 PlaceInfo 객체로 변환한다.
    private Optional<PlaceInfo> toPlaceInfo(String line) {
        List<String> columns = parseCsvLine(line);
        if (columns.size() < 8 || HEADER.equals(line)) {
            return Optional.empty();
        }

        return Optional.of(PlaceInfo.builder()
                .kakaoPlaceId(columns.get(0))
                .placeName(columns.get(1))
                .latitude(parseDouble(columns.get(2)))
                .longitude(parseDouble(columns.get(3)))
                .phone(columns.get(4))
                .address(columns.get(5))
                .kakaoMapUrl(columns.get(6))
                .groupName(columns.get(7))
                .build());
    }

    // 쉼표와 따옴표 이스케이프를 고려해 CSV 한 줄을 컬럼 목록으로 분리한다.
    private List<String> parseCsvLine(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;

        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                columns.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }

        columns.add(current.toString());
        return columns;
    }

    // PlaceInfo 객체를 CSV 한 줄 문자열로 변환한다.
    private String toCsvLine(PlaceInfo placeInfo) {
        return String.join(",",
                escape(placeInfo.getKakaoPlaceId()),
                escape(placeInfo.getPlaceName()),
                escape(toString(placeInfo.getLatitude())),
                escape(toString(placeInfo.getLongitude())),
                escape(placeInfo.getPhone()),
                escape(placeInfo.getAddress()),
                escape(placeInfo.getKakaoMapUrl()),
                escape(placeInfo.getGroupName())
        );
    }

    // CSV 특수문자가 포함된 값을 따옴표로 감싸고 내부 따옴표를 이스케이프한다.
    private String escape(String value) {
        String safeValue = value == null ? "" : value;
        if (safeValue.contains(",") || safeValue.contains("\"") || safeValue.contains("\n")) {
            return "\"" + safeValue.replace("\"", "\"\"") + "\"";
        }
        return safeValue;
    }

    // Double 값을 CSV에 저장 가능한 문자열로 변환한다.
    private String toString(Double value) {
        return value == null ? "" : value.toString();
    }

    // 장소가 기준 좌표로부터 지정 반경 안에 있는지 확인한다.
    private boolean isWithinRadius(Double latitude, Double longitude, PlaceInfo place, int radius) {
        return place.getLatitude() != null
                && place.getLongitude() != null
                && GeoUtils.distanceInMeters(latitude, longitude, place.getLatitude(), place.getLongitude()) <= radius;
    }

    // 기준 좌표와 장소 사이의 거리를 계산한다.
    private double distanceFrom(Double latitude, Double longitude, PlaceInfo place) {
        return GeoUtils.distanceInMeters(latitude, longitude, place.getLatitude(), place.getLongitude());
    }

    // 저장 가능한 최소 식별 정보가 있는지 확인한다.
    private boolean hasMinimumIdentity(PlaceInfo placeInfo) {
        return StringUtils.hasText(placeInfo.getKakaoPlaceId())
                || (StringUtils.hasText(placeInfo.getPlaceName())
                && placeInfo.getLatitude() != null
                && placeInfo.getLongitude() != null);
    }

    // kakaoPlaceId 또는 이름/좌표/주소 조합으로 같은 장소인지 판단한다.
    private boolean isSamePlace(PlaceInfo left, PlaceInfo right) {
        if (StringUtils.hasText(left.getKakaoPlaceId())
                && StringUtils.hasText(right.getKakaoPlaceId())
                && left.getKakaoPlaceId().equals(right.getKakaoPlaceId())) {
            return true;
        }
        if (StringUtils.hasText(left.getKakaoPlaceId()) && StringUtils.hasText(right.getKakaoPlaceId())) {
            return false;
        }

        return normalize(left.getPlaceName()).equals(normalize(right.getPlaceName()))
                && sameCoordinate(left.getLatitude(), right.getLatitude())
                && sameCoordinate(left.getLongitude(), right.getLongitude())
                && Objects.equals(normalize(left.getAddress()), normalize(right.getAddress()));
    }

    // 좌표 오차를 고려해 두 좌표값이 같은지 판단한다.
    private boolean sameCoordinate(Double left, Double right) {
        return left != null && right != null && Math.abs(left - right) < 0.00001;
    }

    // 기존 장소 정보와 새 장소 정보를 병합한다.
    private PlaceInfo merge(PlaceInfo existing, PlaceInfo incoming) {
        return PlaceInfo.builder()
                .kakaoPlaceId(firstText(incoming.getKakaoPlaceId(), existing.getKakaoPlaceId()))
                .placeName(firstText(incoming.getPlaceName(), existing.getPlaceName()))
                .latitude(incoming.getLatitude() != null ? incoming.getLatitude() : existing.getLatitude())
                .longitude(incoming.getLongitude() != null ? incoming.getLongitude() : existing.getLongitude())
                .phone(firstText(incoming.getPhone(), existing.getPhone()))
                .address(firstText(incoming.getAddress(), existing.getAddress()))
                .kakaoMapUrl(firstText(incoming.getKakaoMapUrl(), existing.getKakaoMapUrl()))
                .groupName(firstText(incoming.getGroupName(), existing.getGroupName()))
                .build();
    }

    // 우선값이 비어 있지 않으면 우선값을, 아니면 대체값을 반환한다.
    private String firstText(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred : fallback;
    }

    // null 문자열을 빈 문자열로 보정한다.
    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    // 장소의 그룹명이 요청 카테고리 또는 카테고리 별칭과 매칭되는지 확인한다.
    private boolean matchesCategory(PlaceInfo place, String category) {
        String normalizedGroupName = PlaceCategory.normalizeForComparison(place.getGroupName());
        List<String> aliases = PlaceCategory.aliasesFor(category);

        return aliases.stream()
                .map(PlaceCategory::normalizeForComparison)
                .anyMatch(normalizedGroupName::contains);
    }

    // 비교를 위해 문자열을 소문자와 trim 기준으로 정규화한다.
    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase() : "";
    }

    // CSV 문자열을 Double로 변환하고 실패하면 null을 반환한다.
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

}
