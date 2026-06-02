package com.yeginamgim.place.service;

import com.yeginamgim.board.dto.PlaceInfo;
import com.yeginamgim.board.entity.BoardEntity;
import com.yeginamgim.board.repository.BoardRepository;
import com.yeginamgim.global.exception.InvalidPlaceRequestException;
import com.yeginamgim.place.dto.request.PlaceSearchRequest;
import com.yeginamgim.place.dto.response.PlaceResponse;
import com.yeginamgim.place.dto.response.PopularPlaceResponse;
import com.yeginamgim.trace.repository.TraceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlaceServiceTest {

    @TempDir
    Path tempDir;

    private final KakaoLocalService kakaoLocalService = mock(KakaoLocalService.class);
    private final BoardRepository boardRepository = mock(BoardRepository.class);
    private final TraceRepository traceRepository = mock(TraceRepository.class);

    @Test
    void nearbyRequiresCategory() throws Exception {
        PlaceService placeService = placeServiceWithCache("""
                kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name
                """);

        assertThatThrownBy(() -> placeService.searchNearbyPlaces(PlaceSearchRequest.builder()
                .latitude(37.4979)
                .longitude(127.0276)
                .build()))
                .isInstanceOf(InvalidPlaceRequestException.class)
                .hasMessage("카테고리는 필수입니다.");
    }

    @Test
    void nearbyRejectsOutOfRangeCoordinates() throws Exception {
        PlaceService placeService = placeServiceWithCache("""
                kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name
                """);

        assertThatThrownBy(() -> placeService.searchNearbyPlaces(PlaceSearchRequest.builder()
                .latitude(91.0)
                .longitude(127.0276)
                .category("cafe")
                .build()))
                .isInstanceOf(InvalidPlaceRequestException.class)
                .hasMessage("위도 또는 경도 범위가 올바르지 않습니다.");

        assertThatThrownBy(() -> placeService.searchNearbyPlaces(PlaceSearchRequest.builder()
                .latitude(37.4979)
                .longitude(-181.0)
                .category("cafe")
                .build()))
                .isInstanceOf(InvalidPlaceRequestException.class)
                .hasMessage("위도 또는 경도 범위가 올바르지 않습니다.");
    }

    @Test
    void nearbyRejectsInvalidRadius() throws Exception {
        PlaceService placeService = placeServiceWithCache("""
                kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name
                """);

        assertThatThrownBy(() -> placeService.searchNearbyPlaces(PlaceSearchRequest.builder()
                .latitude(37.4979)
                .longitude(127.0276)
                .category("cafe")
                .radius(0)
                .build()))
                .isInstanceOf(InvalidPlaceRequestException.class)
                .hasMessage("반경은 1m 이상 20000m 이하여야 합니다.");

        assertThatThrownBy(() -> placeService.searchNearbyPlaces(PlaceSearchRequest.builder()
                .latitude(37.4979)
                .longitude(127.0276)
                .category("cafe")
                .radius(20001)
                .build()))
                .isInstanceOf(InvalidPlaceRequestException.class)
                .hasMessage("반경은 1m 이상 20000m 이하여야 합니다.");
    }

    @Test
    void nearbyReturnsBoardPlacesFromCacheBeforeCallingKakao() throws Exception {
        PlaceService placeService = placeServiceWithCache("""
                kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name
                cache-board,Cache Board Cafe,37.4979,127.0276,02-0000-0000,Seoul,https://place.map.kakao.com/cache-board,cafe
                cache-no-board,Cache No Board Cafe,37.4980,127.0277,02-1111-1111,Seoul,https://place.map.kakao.com/cache-no-board,cafe
                """);
        BoardEntity board = BoardEntity.builder()
                .boardId(7L)
                .kakaoPlaceId("cache-board")
                .createdAt(LocalDateTime.now())
                .build();
        when(boardRepository.findByKakaoPlaceIdIn(anyCollection())).thenReturn(List.of(board));
        when(traceRepository.countActiveByKakaoPlaceIds(anyCollection())).thenReturn(List.of(
                placeTraceCount("cache-board", 9L)
        ));

        List<PlaceResponse> responses = placeService.searchNearbyPlaces(PlaceSearchRequest.builder()
                .latitude(37.4979)
                .longitude(127.0276)
                .category("cafe")
                .radius(100)
                .limit(1)
                .build());

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getKakaoPlaceId()).isEqualTo("cache-board");
        assertThat(responses.get(0).getBoardId()).isEqualTo(7L);
        assertThat(responses.get(0).getTraceCount()).isEqualTo(9L);
        verify(kakaoLocalService, never()).searchByCategory(org.mockito.ArgumentMatchers.any());
        verify(boardRepository, never()).findByKakaoPlaceId(anyString());
        verify(traceRepository, never()).countActiveByKakaoPlaceId(anyString());
    }

    @Test
    void nearbyFillsShortCacheResultsWithKakaoAndRemovesDuplicates() throws Exception {
        PlaceService placeService = placeServiceWithCache("""
                kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name
                cache-board,Cache Board Cafe,37.4979,127.0276,02-0000-0000,Seoul,https://place.map.kakao.com/cache-board,cafe
                """);
        when(boardRepository.findByKakaoPlaceIdIn(anyCollection())).thenReturn(List.of(BoardEntity.builder()
                .boardId(7L)
                .kakaoPlaceId("cache-board")
                .createdAt(LocalDateTime.now())
                .build()));
        when(traceRepository.countActiveByKakaoPlaceIds(anyCollection())).thenReturn(List.of(
                placeTraceCount("cache-board", 3L)
        ));
        when(kakaoLocalService.searchByCategory(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(
                PlaceInfo.builder()
                        .kakaoPlaceId("cache-board")
                        .placeName("Duplicate Cafe")
                        .latitude(37.4979)
                        .longitude(127.0276)
                        .groupName("cafe")
                        .build(),
                PlaceInfo.builder()
                        .kakaoPlaceId("kakao-new")
                        .placeName("Kakao New Cafe")
                        .latitude(37.4981)
                        .longitude(127.0278)
                        .groupName("cafe")
                        .build()
        ));

        List<PlaceResponse> responses = placeService.searchNearbyPlaces(PlaceSearchRequest.builder()
                .latitude(37.4979)
                .longitude(127.0276)
                .category("cafe")
                .radius(100)
                .limit(20)
                .build());

        assertThat(responses).extracting(PlaceResponse::getKakaoPlaceId)
                .containsExactly("cache-board", "kakao-new");
    }

    @Test
    void categorySearchDefaultsToOneKilometerRadius() throws Exception {
        PlaceService placeService = placeServiceWithCache("""
                kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name
                """);
        when(kakaoLocalService.searchByCategory(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());

        placeService.searchNearbyPlaces(PlaceSearchRequest.builder()
                .latitude(37.4979)
                .longitude(127.0276)
                .category("cafe")
                .limit(20)
                .build());

        verify(kakaoLocalService).searchByCategory(argThat(request -> request.getRadius() == 1000));
    }

    @Test
    void popularPlacesUseCacheOnly() throws Exception {
        PlaceService placeService = placeServiceWithCache("""
                kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name
                cache-board,Cache Board Cafe,37.4979,127.0276,02-0000-0000,Seoul,https://place.map.kakao.com/cache-board,cafe
                """);
        when(traceRepository.countActiveTracesByPlace()).thenReturn(List.of(placeTraceCount("cache-board", 5L)));
        when(boardRepository.findByKakaoPlaceIdIn(anyCollection())).thenReturn(List.of());

        List<PopularPlaceResponse> responses = placeService.getPopularPlaces(10);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getKakaoPlaceId()).isEqualTo("cache-board");
        verify(kakaoLocalService, never()).findByKakaoPlaceId("cache-board");
    }

    @Test
    void popularPlacesApplyLimitAfterSkippingUncachedPlaces() throws Exception {
        PlaceService placeService = placeServiceWithCache("""
                kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name
                cached-1,Cached One,37.4979,127.0276,02-0000-0000,Seoul,https://place.map.kakao.com/cached-1,cafe
                cached-2,Cached Two,37.4980,127.0277,02-1111-1111,Seoul,https://place.map.kakao.com/cached-2,cafe
                """);
        when(traceRepository.countActiveTracesByPlace()).thenReturn(List.of(
                placeTraceCount("missing-1", 10L),
                placeTraceCount("cached-1", 9L),
                placeTraceCount("missing-2", 8L),
                placeTraceCount("cached-2", 7L)
        ));
        when(boardRepository.findByKakaoPlaceIdIn(anyCollection())).thenReturn(List.of());

        List<PopularPlaceResponse> responses = placeService.getPopularPlaces(2);

        assertThat(responses).extracting(PopularPlaceResponse::getKakaoPlaceId)
                .containsExactly("cached-1", "cached-2");
    }

    private PlaceService placeServiceWithCache(String csv) throws Exception {
        Path cacheFile = tempDir.resolve("places-cache.csv");
        Files.writeString(cacheFile, csv);
        PlaceCsvStore placeCsvStore = new PlaceCsvStore(cacheFile.toString());
        return new PlaceService(kakaoLocalService, boardRepository, traceRepository, placeCsvStore);
    }

    private TraceRepository.PlaceTraceCount placeTraceCount(String kakaoPlaceId, Long traceCount) {
        return new TraceRepository.PlaceTraceCount() {
            @Override
            public String getKakaoPlaceId() {
                return kakaoPlaceId;
            }

            @Override
            public Long getTraceCount() {
                return traceCount;
            }
        };
    }
}
