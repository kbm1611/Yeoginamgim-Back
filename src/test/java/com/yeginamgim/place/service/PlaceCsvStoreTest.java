package com.yeginamgim.place.service;

import com.yeginamgim.board.dto.PlaceInfo;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceCsvStoreTest {

    @Test
    void sampleCsvReadsKoreanPlaceDataAsUtf8() {
        PlaceCsvStore placeCsvStore = new PlaceCsvStore(Path.of("..", "data", "places-cache.csv").toString());

        PlaceInfo placeInfo = placeCsvStore.findByKakaoPlaceId("26338954").orElseThrow();

        assertThat(placeInfo.getPlaceName()).isEqualTo("샘플_스타벅스 강남점");
        assertThat(placeInfo.getAddress()).isEqualTo("서울 강남구 샘플로 1");
        assertThat(placeInfo.getGroupName()).isEqualTo("카페");
    }

    @Test
    void sampleCsvKoreanCategoryAliasesWorkForNearbySearch() {
        PlaceCsvStore placeCsvStore = new PlaceCsvStore(Path.of("..", "data", "places-cache.csv").toString());

        List<PlaceInfo> places = placeCsvStore.findNearby(37.4979, 127.0276, "cafe", 20000);

        assertThat(places).extracting(PlaceInfo::getKakaoPlaceId)
                .contains("26338954", "26338953", "75373753");
    }
}
