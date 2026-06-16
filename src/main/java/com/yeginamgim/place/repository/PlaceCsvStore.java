package com.yeginamgim.place.repository;

import com.yeginamgim.board.dto.PlaceInfo;
import com.yeginamgim.place.util.GeoUtils;
import com.yeginamgim.place.util.PlaceCategory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class PlaceCsvStore {

    private static final String HEADER = "kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name";
    private final PlaceCacheStorage storage;

    public PlaceCsvStore(PlaceCacheStorage storage) {
        this.storage = storage;
        this.storage.ensureExists(HEADER + System.lineSeparator());
    }

    public Optional<PlaceInfo> findByKakaoPlaceId(String kakaoPlaceId) {
        if (!StringUtils.hasText(kakaoPlaceId)) {
            return Optional.empty();
        }

        return findAll().stream()
                .filter(place -> kakaoPlaceId.equals(place.getKakaoPlaceId()))
                .findFirst();
    }

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

    public List<PlaceInfo> findAll() {
        return storage.read()
                .lines()
                .filter(line -> StringUtils.hasText(line) && !line.startsWith("#"))
                .skip(1)
                .map(this::toPlaceInfo)
                .flatMap(Optional::stream)
                .toList();
    }

    private void writeAll(List<PlaceInfo> places) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        places.stream()
                .sorted(Comparator.comparing(place -> defaultString(place.getKakaoPlaceId())))
                .map(this::toCsvLine)
                .forEach(lines::add);

        storage.write(String.join(System.lineSeparator(), lines) + System.lineSeparator());
    }

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

    private String escape(String value) {
        String safeValue = value == null ? "" : value;
        if (safeValue.contains(",") || safeValue.contains("\"") || safeValue.contains("\n")) {
            return "\"" + safeValue.replace("\"", "\"\"") + "\"";
        }
        return safeValue;
    }

    private String toString(Double value) {
        return value == null ? "" : value.toString();
    }

    private boolean isWithinRadius(Double latitude, Double longitude, PlaceInfo place, int radius) {
        return place.getLatitude() != null
                && place.getLongitude() != null
                && GeoUtils.distanceInMeters(latitude, longitude, place.getLatitude(), place.getLongitude()) <= radius;
    }

    private double distanceFrom(Double latitude, Double longitude, PlaceInfo place) {
        return GeoUtils.distanceInMeters(latitude, longitude, place.getLatitude(), place.getLongitude());
    }

    private boolean hasMinimumIdentity(PlaceInfo placeInfo) {
        return StringUtils.hasText(placeInfo.getKakaoPlaceId())
                || (StringUtils.hasText(placeInfo.getPlaceName())
                && placeInfo.getLatitude() != null
                && placeInfo.getLongitude() != null);
    }

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

    private boolean sameCoordinate(Double left, Double right) {
        return left != null && right != null && Math.abs(left - right) < 0.00001;
    }

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

    private String firstText(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred : fallback;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private boolean matchesCategory(PlaceInfo place, String category) {
        return PlaceCategory.matchesSelectedCategory(place.getPlaceName(), place.getGroupName(), category);
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase() : "";
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
}
