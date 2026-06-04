package com.yeginamgim.place.util;

import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;

public final class PlaceCategory {

    private static final Map<String, List<String>> ALIASES_BY_KAKAO_CODE = Map.ofEntries(
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
    private static final Map<String, String> KAKAO_CODE_BY_ALIAS = buildKakaoCodeByAlias();

    private PlaceCategory() {
    }

    public static Optional<String> toKakaoCategoryCode(String category) {
        if (!StringUtils.hasText(category)) {
            return Optional.empty();
        }

        return Optional.ofNullable(KAKAO_CODE_BY_ALIAS.get(normalizeForComparison(category)));
    }

    public static List<String> aliasesFor(String category) {
        return toKakaoCategoryCode(category)
                .map(code -> ALIASES_BY_KAKAO_CODE.getOrDefault(code, List.of()))
                .orElse(List.of());
    }

    public static String normalizeForComparison(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replace(" ", "");
    }

    private static Map<String, String> buildKakaoCodeByAlias() {
        Map<String, String> codeByAlias = new HashMap<>();
        ALIASES_BY_KAKAO_CODE.forEach((code, aliases) -> {
            codeByAlias.put(normalizeForComparison(code), code);
            aliases.forEach(alias -> codeByAlias.put(normalizeForComparison(alias), code));
        });
        return Map.copyOf(codeByAlias);
    }
}
