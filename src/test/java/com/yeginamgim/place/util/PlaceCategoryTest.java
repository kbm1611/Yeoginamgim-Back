package com.yeginamgim.place.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceCategoryTest {

    @Test
    void serviceCategoriesKeepRequestedPriorityOrder() {
        assertThat(PlaceCategory.orderedCategoryKeys()).containsExactly(
                "CE7",
                "FD6",
                "CS2",
                "PARK",
                "CULTURE",
                "SHOPPING",
                "AT4",
                "EDU",
                "MT1",
                "AD5"
        );
    }

    @Test
    void directKakaoCategoriesUseOfficialCategoryGroupCodes() {
        assertThat(PlaceCategory.kakaoCategoryCodesFor("cafe")).containsExactly("CE7");
        assertThat(PlaceCategory.kakaoCategoryCodesFor("\uC74C\uC2DD\uC810 / \uB9DB\uC9D1"))
                .containsExactly("FD6");
        assertThat(PlaceCategory.kakaoCategoryCodesFor("\uD3B8\uC758\uC810")).containsExactly("CS2");
        assertThat(PlaceCategory.kakaoCategoryCodesFor("\uB9C8\uD2B8")).containsExactly("MT1");
        assertThat(PlaceCategory.kakaoCategoryCodesFor("\uC219\uBC15 / \uD638\uD154"))
                .containsExactly("AD5");
    }

    @Test
    void educationUsesSchoolAndAcademyCategoryCodesInOrder() {
        assertThat(PlaceCategory.kakaoCategoryCodesFor("\uD559\uAD50 / \uD559\uC6D0"))
                .containsExactly("SC4", "AC5");
    }

    @Test
    void ambiguousCategoriesExposeKeywordFallbacks() {
        assertThat(PlaceCategory.keywordQueriesFor("\uACF5\uC6D0 / \uC0B0\uCC45\uB85C"))
                .contains("\uACF5\uC6D0", "\uC0B0\uCC45\uB85C");
        assertThat(PlaceCategory.keywordQueriesFor("\uBB38\uD654\uC2DC\uC124 / \uC804\uC2DC / \uD31D\uC5C5"))
                .contains("\uC804\uC2DC", "\uD31D\uC5C5\uC2A4\uD1A0\uC5B4");
        assertThat(PlaceCategory.keywordQueriesFor("\uC1FC\uD551 / \uC18C\uD488\uC0F5 / \uD3B8\uC9D1\uC0F5"))
                .contains("\uC18C\uD488\uC0F5", "\uD3B8\uC9D1\uC0F5");
        assertThat(PlaceCategory.keywordQueriesFor("\uAD00\uAD11\uBA85\uC18C / \uD3EC\uD1A0\uC2A4\uD31F"))
                .contains("\uD3EC\uD1A0\uC2A4\uD31F", "\uC0AC\uC9C4\uBA85\uC18C");
    }

    @Test
    void categoryMatchingUsesPlaceNameForAmbiguousKeywordCategories() {
        assertThat(PlaceCategory.matchesSelectedCategory(
                "\uC11C\uC6B8\uC232 \uACF5\uC6D0",
                "\uAD00\uAD11\uBA85\uC18C",
                "PARK"
        )).isTrue();
        assertThat(PlaceCategory.matchesSelectedCategory(
                "\uCEE4\uD53C \uC0C1\uC810",
                "\uCE74\uD398",
                "PARK"
        )).isFalse();
    }
}
