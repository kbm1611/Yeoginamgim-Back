package com.yeginamgim.place.util;

import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Map.entry;

public final class PlaceCategory {

    private static final List<CategoryProfile> CATEGORY_PROFILES = List.of(
            new CategoryProfile("CE7",
                    List.of("CE7", "cafe", "coffee", "\uCE74\uD398", "\uCEE4\uD53C"),
                    List.of("CE7"),
                    List.of()),
            new CategoryProfile("FD6",
                    List.of("FD6", "food", "restaurant", "dining",
                            "\uC74C\uC2DD", "\uC74C\uC810", "\uC74C\uC2DD\uC810",
                            "\uB9DB\uC9D1", "\uC2DD\uB2F9", "\uC74C\uC2DD\uC810/\uB9DB\uC9D1"),
                    List.of("FD6"),
                    List.of()),
            new CategoryProfile("CS2",
                    List.of("CS2", "convenience", "convenience_store", "store", "\uD3B8\uC758\uC810"),
                    List.of("CS2"),
                    List.of()),
            new CategoryProfile("PARK",
                    List.of("PARK", "park", "trail", "walk", "walking_trail",
                            "\uACF5\uC6D0", "\uC0B0\uCC45\uB85C", "\uB458\uB808\uAE38",
                            "\uACF5\uC6D0/\uC0B0\uCC45\uB85C"),
                    List.of(),
                    List.of("\uACF5\uC6D0", "\uC0B0\uCC45\uB85C", "\uB458\uB808\uAE38")),
            new CategoryProfile("CULTURE",
                    List.of("CULTURE", "CT1", "culture", "cultural_facility", "exhibition", "popup",
                            "\uBB38\uD654", "\uBB38\uD654\uC2DC\uC124", "\uC804\uC2DC",
                            "\uD31D\uC5C5", "\uD31D\uC5C5\uC2A4\uD1A0\uC5B4",
                            "\uBB38\uD654\uC2DC\uC124/\uC804\uC2DC/\uD31D\uC5C5"),
                    List.of("CT1"),
                    List.of("\uC804\uC2DC", "\uD31D\uC5C5\uC2A4\uD1A0\uC5B4", "\uBBF8\uC220\uAD00",
                            "\uBC15\uBB3C\uAD00")),
            new CategoryProfile("SHOPPING",
                    List.of("SHOPPING", "shopping", "shop", "select_shop", "lifestyle_shop",
                            "\uC1FC\uD551", "\uC18C\uD488\uC0F5", "\uD3B8\uC9D1\uC0F5",
                            "\uC0C1\uC810", "\uC1FC\uD551/\uC18C\uD488\uC0F5/\uD3B8\uC9D1\uC0F5"),
                    List.of(),
                    List.of("\uC18C\uD488\uC0F5", "\uD3B8\uC9D1\uC0F5", "\uC1FC\uD551")),
            new CategoryProfile("AT4",
                    List.of("AT4", "attraction", "tour", "tourist_attraction", "photo_spot",
                            "\uAD00\uAD11", "\uAD00\uAD11\uBA85\uC18C", "\uBA85\uC18C",
                            "\uD3EC\uD1A0\uC2A4\uD31F", "\uC0AC\uC9C4\uBA85\uC18C",
                            "\uAD00\uAD11\uBA85\uC18C/\uD3EC\uD1A0\uC2A4\uD31F"),
                    List.of("AT4"),
                    List.of("\uD3EC\uD1A0\uC2A4\uD31F", "\uC0AC\uC9C4\uBA85\uC18C")),
            new CategoryProfile("EDU",
                    List.of("EDU", "school", "academy", "hagwon",
                            "\uD559\uAD50", "\uD559\uC6D0", "\uD559\uAD50/\uD559\uC6D0"),
                    List.of("SC4", "AC5"),
                    List.of()),
            new CategoryProfile("MT1",
                    List.of("MT1", "mart", "large_mart", "largemart", "market", "supermarket",
                            "\uB300\uD615\uB9C8\uD2B8", "\uB9C8\uD2B8"),
                    List.of("MT1"),
                    List.of()),
            new CategoryProfile("AD5",
                    List.of("AD5", "lodging", "accommodation", "hotel",
                            "\uC219\uBC15", "\uD638\uD154", "\uC219\uBC15/\uD638\uD154"),
                    List.of("AD5"),
                    List.of())
    );
    private static final Map<String, CategoryProfile> PROFILE_BY_ALIAS = buildProfileByAlias();
    private static final Set<String> OFFICIAL_KAKAO_CODES = Set.of(
            "MT1", "CS2", "PS3", "SC4", "AC5", "PK6", "OL7", "SW8", "BK9",
            "CT1", "AG2", "PO3", "AT4", "AD5", "FD6", "CE7", "HP8", "PM9"
    );
    private static final Map<String, List<String>> LEGACY_ALIASES_BY_KAKAO_CODE = Map.ofEntries(
            entry("MT1", List.of("MT1", "mart", "large_mart", "largemart", "market", "supermarket",
                    "\uB300\uD615\uB9C8\uD2B8")),
            entry("CS2", List.of("CS2", "convenience", "convenience_store", "store",
                    "\uD3B8\uC758\uC810")),
            entry("PS3", List.of("PS3", "preschool", "kindergarten",
                    "\uC5B4\uB9B0\uC774\uC9D1",
                    "\uC720\uCE58\uC6D0",
                    "\uC5B4\uB9B0\uC774\uC9D1,\uC720\uCE58\uC6D0")),
            entry("SC4", List.of("SC4", "school", "\uD559\uAD50")),
            entry("AC5", List.of("AC5", "academy", "hagwon", "\uD559\uC6D0")),
            entry("PK6", List.of("PK6", "parking", "\uC8FC\uCC28\uC7A5")),
            entry("OL7", List.of("OL7", "oil", "gas", "charging",
                    "\uC8FC\uC720\uC18C",
                    "\uCDA9\uC804\uC18C",
                    "\uC8FC\uC720\uC18C,\uCDA9\uC804\uC18C")),
            entry("SW8", List.of("SW8", "subway", "subway_station",
                    "\uC9C0\uD558\uCCA0\uC5ED")),
            entry("BK9", List.of("BK9", "bank", "\uC740\uD589")),
            entry("CT1", List.of("CT1", "culture", "cultural_facility",
                    "\uBB38\uD654",
                    "\uBB38\uD654\uC2DC\uC124",
                    "\uC804\uC2DC")),
            entry("AG2", List.of("AG2", "real_estate", "realestate",
                    "\uC911\uAC1C\uC5C5\uC18C",
                    "\uBD80\uB3D9\uC0B0")),
            entry("PO3", List.of("PO3", "public", "public_institution", "institution",
                    "\uACF5\uACF5\uAE30\uAD00")),
            entry("AT4", List.of("AT4", "attraction", "tour", "tourist_attraction",
                    "\uAD00\uAD11",
                    "\uAD00\uAD11\uBA85\uC18C",
                    "\uBA85\uC18C")),
            entry("AD5", List.of("AD5", "lodging", "accommodation", "hotel",
                    "\uC219\uBC15")),
            entry("FD6", List.of("FD6", "food", "restaurant", "dining",
                    "\uC74C\uC2DD",
                    "\uC74C\uC2DD\uC810",
                    "\uB9DB\uC9D1",
                    "\uC2DD\uB2F9")),
            entry("CE7", List.of("CE7", "cafe", "coffee",
                    "\uCE74\uD398",
                    "\uCEE4\uD53C")),
            entry("HP8", List.of("HP8", "hospital", "\uBCD1\uC6D0")),
            entry("PM9", List.of("PM9", "pharmacy", "\uC57D\uAD6D"))
    );

    private PlaceCategory() {
    }

    public static List<String> orderedCategoryKeys() {
        return CATEGORY_PROFILES.stream()
                .map(CategoryProfile::key)
                .toList();
    }

    public static Optional<String> toServiceCategoryKey(String category) {
        if (!StringUtils.hasText(category)) {
            return Optional.empty();
        }

        CategoryProfile profile = PROFILE_BY_ALIAS.get(normalizeForComparison(category));
        return Optional.ofNullable(profile)
                .map(CategoryProfile::key);
    }

    public static Optional<String> toExactKakaoCategoryCode(String category) {
        if (!StringUtils.hasText(category)) {
            return Optional.empty();
        }

        String officialCode = normalizeForComparison(category).toUpperCase(Locale.ROOT);
        return OFFICIAL_KAKAO_CODES.contains(officialCode) ? Optional.of(officialCode) : Optional.empty();
    }

    public static Optional<String> toKakaoCategoryCode(String category) {
        return kakaoCategoryCodesFor(category).stream().findFirst();
    }

    public static List<String> kakaoCategoryCodesFor(String category) {
        if (!StringUtils.hasText(category)) {
            return List.of();
        }

        String normalizedCategory = normalizeForComparison(category);
        Optional<String> exactKakaoCode = toExactKakaoCategoryCode(category);
        if (exactKakaoCode.isPresent()) {
            return List.of(exactKakaoCode.get());
        }

        CategoryProfile profile = PROFILE_BY_ALIAS.get(normalizedCategory);
        return profile == null ? List.of() : profile.kakaoCategoryCodes();
    }

    public static List<String> keywordQueriesFor(String category) {
        if (!StringUtils.hasText(category)) {
            return List.of();
        }

        String normalizedCategory = normalizeForComparison(category);
        if (toExactKakaoCategoryCode(category).isPresent()) {
            return List.of();
        }

        CategoryProfile profile = PROFILE_BY_ALIAS.get(normalizedCategory);
        return profile == null ? List.of() : profile.keywordQueries();
    }

    public static List<String> aliasesFor(String category) {
        if (!StringUtils.hasText(category)) {
            return List.of();
        }

        String normalizedCategory = normalizeForComparison(category);
        Optional<String> exactKakaoCode = toExactKakaoCategoryCode(category);
        if (exactKakaoCode.isPresent()) {
            return LEGACY_ALIASES_BY_KAKAO_CODE.getOrDefault(
                    exactKakaoCode.get(),
                    List.of(exactKakaoCode.get())
            );
        }

        CategoryProfile profile = PROFILE_BY_ALIAS.get(normalizedCategory);
        return profile == null ? List.of() : profile.aliases();
    }

    public static boolean matchesSelectedCategory(String placeName, String groupName, String category) {
        List<String> aliases = aliasesFor(category);
        if (aliases.isEmpty()) {
            return false;
        }

        String source = normalizeForComparison(defaultString(placeName) + " " + defaultString(groupName));
        return aliases.stream()
                .map(PlaceCategory::normalizeForComparison)
                .anyMatch(source::contains);
    }

    public static String normalizeForComparison(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[\\s/_\\-,.·]+", "");
    }

    private static Map<String, CategoryProfile> buildProfileByAlias() {
        Map<String, CategoryProfile> profileByAlias = new HashMap<>();
        CATEGORY_PROFILES.forEach(profile -> {
            profileByAlias.put(normalizeForComparison(profile.key()), profile);
            profile.aliases().forEach(alias -> profileByAlias.put(normalizeForComparison(alias), profile));
            profile.kakaoCategoryCodes().forEach(code -> profileByAlias.putIfAbsent(normalizeForComparison(code), profile));
        });
        return Map.copyOf(profileByAlias);
    }

    private static String defaultString(String value) {
        return value == null ? "" : value;
    }

    private record CategoryProfile(
            String key,
            List<String> aliases,
            List<String> kakaoCategoryCodes,
            List<String> keywordQueries
    ) {
    }
}
