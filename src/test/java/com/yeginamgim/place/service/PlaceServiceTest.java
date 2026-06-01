package com.yeginamgim.place.service;

import com.yeginamgim.board.dto.PlaceInfo;
import com.yeginamgim.board.entity.BoardEntity;
import com.yeginamgim.board.repository.BoardRepository;
import com.yeginamgim.place.dto.request.PlaceConfirmRequest;
import com.yeginamgim.place.dto.request.PlaceSearchRequest;
import com.yeginamgim.place.dto.response.PlaceResponse;
import com.yeginamgim.place.dto.response.PopularPlaceResponse;
import com.yeginamgim.trace.repository.TraceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.argThat;

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
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
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
        when(boardRepository.findByKakaoPlaceId("cache-board")).thenReturn(Optional.of(board));
        when(boardRepository.findByKakaoPlaceId("cache-no-board")).thenReturn(Optional.empty());
        when(traceRepository.countActiveByKakaoPlaceId("cache-board")).thenReturn(9L);
        when(traceRepository.countActiveByKakaoPlaceId("cache-no-board")).thenReturn(0L);

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
    }

    @Test
    void nearbyFillsShortCacheResultsWithKakaoAndRemovesDuplicates() throws Exception {
        PlaceService placeService = placeServiceWithCache("""
                kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name
                cache-board,Cache Board Cafe,37.4979,127.0276,02-0000-0000,Seoul,https://place.map.kakao.com/cache-board,cafe
                """);
        when(boardRepository.findByKakaoPlaceId("cache-board")).thenReturn(Optional.of(BoardEntity.builder()
                .boardId(7L)
                .kakaoPlaceId("cache-board")
                .createdAt(LocalDateTime.now())
                .build()));
        when(boardRepository.findByKakaoPlaceId("kakao-new")).thenReturn(Optional.empty());
        when(traceRepository.countActiveByKakaoPlaceId("cache-board")).thenReturn(3L);
        when(traceRepository.countActiveByKakaoPlaceId("kakao-new")).thenReturn(0L);
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
    void clickCandidatesReturnCacheWithoutCallingKakao() throws Exception {
        PlaceService placeService = placeServiceWithCache("""
                kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name
                cache-near,Cache Near Place,37.4979,127.0276,02-0000-0000,Seoul,https://place.map.kakao.com/cache-near,cafe
                cache-far,Cache Far Place,37.5100,127.0400,02-1111-1111,Seoul,https://place.map.kakao.com/cache-far,cafe
                """);
        when(boardRepository.findByKakaoPlaceId("cache-near")).thenReturn(Optional.empty());
        when(traceRepository.countActiveByKakaoPlaceId("cache-near")).thenReturn(0L);

        List<PlaceResponse> responses = placeService.searchClickCandidates(PlaceSearchRequest.builder()
                .latitude(37.4979)
                .longitude(127.0276)
                .radius(100)
                .limit(20)
                .build());

        assertThat(responses).extracting(PlaceResponse::getKakaoPlaceId)
                .containsExactly("cache-near");
        verify(kakaoLocalService, never()).searchAround(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void clickCandidatesUseKakaoAsTemporaryResultsWhenCacheIsEmpty() throws Exception {
        Path cacheFile = tempDir.resolve("places-cache.csv");
        Files.writeString(cacheFile, "kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name\n");
        PlaceCsvStore placeCsvStore = new PlaceCsvStore(cacheFile.toString());
        PlaceService placeService = new PlaceService(kakaoLocalService, boardRepository, traceRepository, placeCsvStore);
        when(boardRepository.findByKakaoPlaceId("kakao-temp")).thenReturn(Optional.empty());
        when(traceRepository.countActiveByKakaoPlaceId("kakao-temp")).thenReturn(0L);
        when(kakaoLocalService.searchAround(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(
                PlaceInfo.builder()
                        .kakaoPlaceId("kakao-temp")
                        .placeName("Kakao Temporary Place")
                        .latitude(37.4979)
                        .longitude(127.0276)
                        .groupName("cafe")
                        .build()
        ));

        List<PlaceResponse> responses = placeService.searchClickCandidates(PlaceSearchRequest.builder()
                .latitude(37.4979)
                .longitude(127.0276)
                .radius(100)
                .limit(20)
                .build());

        assertThat(responses).extracting(PlaceResponse::getKakaoPlaceId)
                .containsExactly("kakao-temp");
        assertThat(Files.readString(cacheFile)).doesNotContain("kakao-temp");
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
    void confirmPlaceCachesSelectedTemporaryCandidate() throws Exception {
        Path cacheFile = tempDir.resolve("places-cache.csv");
        Files.writeString(cacheFile, "kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name\n");
        PlaceCsvStore placeCsvStore = new PlaceCsvStore(cacheFile.toString());
        PlaceService placeService = new PlaceService(kakaoLocalService, boardRepository, traceRepository, placeCsvStore);
        when(boardRepository.findByKakaoPlaceId("selected-place")).thenReturn(Optional.empty());
        when(traceRepository.countActiveByKakaoPlaceId("selected-place")).thenReturn(0L);

        PlaceResponse response = placeService.confirmPlace(PlaceConfirmRequest.builder()
                .kakaoPlaceId("selected-place")
                .placeName("Selected Cafe")
                .latitude(37.4979)
                .longitude(127.0276)
                .phone("02-0000-0000")
                .address("Seoul")
                .kakaoMapUrl("https://place.map.kakao.com/selected-place")
                .groupName("cafe")
                .build());

        assertThat(response.getKakaoPlaceId()).isEqualTo("selected-place");
        assertThat(Files.readString(cacheFile)).contains("selected-place,Selected Cafe");
    }

    @Test
    void findsPlaceFromCacheOnlyByKakaoPlaceId() throws Exception {
        PlaceService placeService = placeServiceWithCache("""
                kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name
                cache-board,Cache Board Cafe,37.4979,127.0276,02-0000-0000,Seoul,https://place.map.kakao.com/cache-board,cafe
                """);
        when(traceRepository.countActiveByKakaoPlaceId("cache-board")).thenReturn(3L);

        PlaceResponse response = placeService.getPlaceByKakaoPlaceId("cache-board");

        assertThat(response.getKakaoPlaceId()).isEqualTo("cache-board");
        assertThat(response.getPlaceName()).isEqualTo("Cache Board Cafe");
        verify(kakaoLocalService, never()).findByKakaoPlaceId("cache-board");
    }

    @Test
    void popularPlacesUseCacheOnly() throws Exception {
        PlaceService placeService = placeServiceWithCache("""
                kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name
                cache-board,Cache Board Cafe,37.4979,127.0276,02-0000-0000,Seoul,https://place.map.kakao.com/cache-board,cafe
                """);
        when(traceRepository.countActiveTracesByPlace()).thenReturn(List.of(placeTraceCount("cache-board", 5L)));

        List<PopularPlaceResponse> responses = placeService.getPopularPlaces(10);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getKakaoPlaceId()).isEqualTo("cache-board");
        verify(kakaoLocalService, never()).findByKakaoPlaceId("cache-board");
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
