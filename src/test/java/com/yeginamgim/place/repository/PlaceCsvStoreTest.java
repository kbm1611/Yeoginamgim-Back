package com.yeginamgim.place.repository;

import com.yeginamgim.board.dto.PlaceInfo;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceCsvStoreTest {

    @Disabled("샘플 데이터가 외부 CSV에 없음 - 해당 파일 관리자 확인 필요")
    @Test
    void sampleCsvReadsKoreanPlaceDataAsUtf8() {
        PlaceCsvStore placeCsvStore = new PlaceCsvStore(Path.of("..", "data", "places-cache.csv").toString());

        PlaceInfo placeInfo = placeCsvStore.findByKakaoPlaceId("26338954").orElseThrow();

        assertThat(placeInfo.getPlaceName()).isEqualTo("샘플_스타벅스 강남점");
        assertThat(placeInfo.getAddress()).isEqualTo("서울 강남구 샘플로 1");
        assertThat(placeInfo.getGroupName()).isEqualTo("카페");
    }

    @Disabled("샘플 데이터가 외부 CSV에 없음 - 해당 파일 관리자 확인 필요")
    @Test
    void sampleCsvKoreanCategoryAliasesWorkForNearbySearch() {
        PlaceCsvStore placeCsvStore = new PlaceCsvStore(Path.of("..", "data", "places-cache.csv").toString());

        List<PlaceInfo> places = placeCsvStore.findNearby(37.4979, 127.0276, "cafe", 20000);

        assertThat(places).extracting(PlaceInfo::getKakaoPlaceId)
                .contains("26338954", "26338953", "75373753");
    }
}
