package com.yeginamgim.place.repository;

import com.yeginamgim.board.dto.PlaceInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceCsvStoreTest {

    private static final String HEADER =
            "kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name";

    @Test
    void readsPlaceDataFromInjectedStorage() {
        InMemoryPlaceCacheStorage storage = new InMemoryPlaceCacheStorage("""
                kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name
                26338954,Sample Cafe,37.4979,127.0276,02-0000-0000,Seoul,http://place.map.kakao.com/26338954,cafe
                """);
        PlaceCsvStore placeCsvStore = new PlaceCsvStore(storage);

        PlaceInfo placeInfo = placeCsvStore.findByKakaoPlaceId("26338954").orElseThrow();

        assertThat(placeInfo.getPlaceName()).isEqualTo("Sample Cafe");
        assertThat(placeInfo.getAddress()).isEqualTo("Seoul");
        assertThat(placeInfo.getGroupName()).isEqualTo("cafe");
    }

    @Test
    void writesMergedPlaceDataBackToInjectedStorage() {
        InMemoryPlaceCacheStorage storage = new InMemoryPlaceCacheStorage(HEADER + System.lineSeparator());
        PlaceCsvStore placeCsvStore = new PlaceCsvStore(storage);

        placeCsvStore.saveIfAbsent(PlaceInfo.builder()
                .kakaoPlaceId("26338954")
                .placeName("Sample Cafe")
                .latitude(37.4979)
                .longitude(127.0276)
                .address("Seoul")
                .groupName("cafe")
                .build());

        assertThat(storage.content())
                .contains("26338954,Sample Cafe,37.4979,127.0276,,Seoul,,cafe");
    }

    @Test
    void categoryAliasesWorkForNearbySearch() {
        InMemoryPlaceCacheStorage storage = new InMemoryPlaceCacheStorage("""
                kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name
                26338954,Sample Cafe,37.4979,127.0276,02-0000-0000,Seoul,http://place.map.kakao.com/26338954,cafe
                75373753,Far Cafe,37.7000,127.3000,02-0000-0000,Seoul,http://place.map.kakao.com/75373753,cafe
                """);
        PlaceCsvStore placeCsvStore = new PlaceCsvStore(storage);

        List<PlaceInfo> places = placeCsvStore.findNearby(37.4979, 127.0276, "cafe", 20000);

        assertThat(places).extracting(PlaceInfo::getKakaoPlaceId)
                .containsExactly("26338954");
    }

    private static class InMemoryPlaceCacheStorage implements PlaceCacheStorage {

        private String content;

        private InMemoryPlaceCacheStorage(String content) {
            this.content = content;
        }

        @Override
        public String read() {
            return content;
        }

        @Override
        public void write(String content) {
            this.content = content;
        }

        @Override
        public void ensureExists(String initialContent) {
            if (content == null || content.isBlank()) {
                content = initialContent;
            }
        }

        private String content() {
            return content;
        }
    }
}
