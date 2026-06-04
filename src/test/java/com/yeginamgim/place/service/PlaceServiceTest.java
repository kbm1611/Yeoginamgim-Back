package com.yeginamgim.place.service;

import com.yeginamgim.board.dto.PlaceInfo;
import com.yeginamgim.board.entity.BoardEntity;
import com.yeginamgim.board.repository.BoardRepository;
import com.yeginamgim.global.exception.InvalidPlaceRequestException;
import com.yeginamgim.place.dto.request.PlaceSearchRequest;
import com.yeginamgim.place.dto.response.PlaceResponse;
import com.yeginamgim.place.dto.response.PopularPlaceResponse;
import com.yeginamgim.place.repository.PlaceCsvStore;
import com.yeginamgim.place.util.PlaceSearchRequestValidator;
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
    void nearbyReturnsCachedPlacesWithoutCallingKakaoWhenCacheIsEnough() throws Exception {
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
    void keywordSearchUsesKakaoKeywordResultsAndEnrichesBoardAndTraceCounts() throws Exception {
        PlaceService placeService = placeServiceWithCache("""
                kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name
                """);
        when(kakaoLocalService.searchByKeyword(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(
                PlaceInfo.builder()
                        .kakaoPlaceId("kakao-coffee")
                        .placeName("Kakao Coffee")
                        .latitude(37.5447)
                        .longitude(127.0559)
                        .groupName("cafe")
                        .build()
        ));
        when(boardRepository.findByKakaoPlaceIdIn(anyCollection())).thenReturn(List.of(BoardEntity.builder()
                .boardId(11L)
                .kakaoPlaceId("kakao-coffee")
                .createdAt(LocalDateTime.now())
                .build()));
        when(traceRepository.countActiveByKakaoPlaceIds(anyCollection())).thenReturn(List.of(
                placeTraceCount("kakao-coffee", 6L)
        ));

        List<PlaceResponse> responses = placeService.searchPlacesByKeyword(PlaceSearchRequest.builder()
                .query("coffee")
                .latitude(37.5447)
                .longitude(127.0559)
                .category("cafe")
                .radius(20000)
                .limit(20)
                .build());

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getKakaoPlaceId()).isEqualTo("kakao-coffee");
        assertThat(responses.get(0).getBoardId()).isEqualTo(11L);
        assertThat(responses.get(0).getTraceCount()).isEqualTo(6L);
        verify(kakaoLocalService).searchByKeyword(argThat(request ->
                "coffee".equals(request.getQuery())
                        && request.getLatitude().equals(37.5447)
                        && request.getLongitude().equals(127.0559)
                        && "CE7".equals(request.getCategory())
                        && request.getRadius() == 1000
                        && request.getLimit() == 15
        ));
        verify(kakaoLocalService, never()).searchByCategory(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void nearbyCapsKakaoLookupToOneKilometerAndFifteenResults() throws Exception {
        PlaceService placeService = placeServiceWithCache("""
                kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name
                """);
        when(kakaoLocalService.searchByCategory(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());

        placeService.searchNearbyPlaces(PlaceSearchRequest.builder()
                .latitude(37.4979)
                .longitude(127.0276)
                .category("cafe")
                .radius(20000)
                .limit(20)
                .build());

        verify(kakaoLocalService).searchByCategory(argThat(request ->
                request.getRadius() == 1000 && request.getLimit() == 15
        ));
    }

    @Test
    void nearbyKeepsDistanceOrderAcrossCacheAndKakaoResults() throws Exception {
        PlaceService placeService = placeServiceWithCache("""
                kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name
                cache-far,Cache Far Cafe,37.5020,127.0276,02-0000-0000,Seoul,https://place.map.kakao.com/cache-far,cafe
                """);
        when(boardRepository.findByKakaoPlaceIdIn(anyCollection())).thenReturn(List.of(BoardEntity.builder()
                .boardId(7L)
                .kakaoPlaceId("cache-far")
                .createdAt(LocalDateTime.now())
                .build()));
        when(traceRepository.countActiveByKakaoPlaceIds(anyCollection())).thenReturn(List.of(
                placeTraceCount("cache-far", 99L)
        ));
        when(kakaoLocalService.searchByCategory(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(
                PlaceInfo.builder()
                        .kakaoPlaceId("kakao-near")
                        .placeName("Kakao Near Cafe")
                        .latitude(37.4979)
                        .longitude(127.0276)
                        .groupName("cafe")
                        .build()
        ));

        List<PlaceResponse> responses = placeService.searchNearbyPlaces(PlaceSearchRequest.builder()
                .latitude(37.4979)
                .longitude(127.0276)
                .category("cafe")
                .limit(2)
                .build());

        assertThat(responses).extracting(PlaceResponse::getKakaoPlaceId)
                .containsExactly("kakao-near", "cache-far");
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

    @Test
    void popularPlacesFilterByDistrictWhenProvided() throws Exception {
        PlaceService placeService = placeServiceWithCache("""
                kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name
                gangnam-1,Gangnam One,37.4979,127.0276,02-0000-0000,서울 강남구 테헤란로 1,https://place.map.kakao.com/gangnam-1,cafe
                seongdong-1,Seongdong One,37.5445,127.0557,02-1111-1111,서울 성동구 성수동,https://place.map.kakao.com/seongdong-1,culture
                gangnam-2,Gangnam Two,37.5006,127.0265,02-2222-2222,서울 강남구 강남대로 2,https://place.map.kakao.com/gangnam-2,cafe
                """);
        when(traceRepository.countActiveTracesByPlace()).thenReturn(List.of(
                placeTraceCount("seongdong-1", 10L),
                placeTraceCount("gangnam-1", 9L),
                placeTraceCount("gangnam-2", 7L)
        ));
        when(boardRepository.findByKakaoPlaceIdIn(anyCollection())).thenReturn(List.of());

        List<PopularPlaceResponse> responses = placeService.getPopularPlaces(10, "강남구");

        assertThat(responses).extracting(PopularPlaceResponse::getKakaoPlaceId)
                .containsExactly("gangnam-1", "gangnam-2");
        assertThat(responses).extracting(PopularPlaceResponse::getRank)
                .containsExactly(1, 2);
    }

    @Test
    void popularPlacesFilterByCurrentLocationRadiusWhenProvided() throws Exception {
        PlaceService placeService = placeServiceWithCache("""
                kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name
                near-1,Near One,37.5447,127.0559,02-0000-0000,서울 성동구 성수동,https://place.map.kakao.com/near-1,cafe
                near-2,Near Two,37.5450,127.0562,02-1111-1111,서울 성동구 성수동,https://place.map.kakao.com/near-2,culture
                far-1,Far One,37.4979,127.0276,02-2222-2222,서울 강남구 역삼동,https://place.map.kakao.com/far-1,cafe
                """);
        when(traceRepository.countActiveTracesByPlace()).thenReturn(List.of(
                placeTraceCount("far-1", 50L),
                placeTraceCount("near-1", 10L),
                placeTraceCount("near-2", 7L)
        ));
        when(boardRepository.findByKakaoPlaceIdIn(anyCollection())).thenReturn(List.of());

        List<PopularPlaceResponse> responses = placeService.getPopularPlaces(
                10,
                null,
                37.5447,
                127.0559,
                500
        );

        assertThat(responses).extracting(PopularPlaceResponse::getKakaoPlaceId)
                .containsExactly("near-1", "near-2");
        assertThat(responses).extracting(PopularPlaceResponse::getRank)
                .containsExactly(1, 2);
    }

    @Test
    void popularPlacesTreatBlankDistrictAsGlobalPopular() throws Exception {
        PlaceService placeService = placeServiceWithCache("""
                kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name
                gangnam-1,Gangnam One,37.4979,127.0276,02-0000-0000,서울 강남구 테헤란로 1,https://place.map.kakao.com/gangnam-1,cafe
                seongdong-1,Seongdong One,37.5445,127.0557,02-1111-1111,서울 성동구 성수동,https://place.map.kakao.com/seongdong-1,culture
                """);
        when(traceRepository.countActiveTracesByPlace()).thenReturn(List.of(
                placeTraceCount("seongdong-1", 10L),
                placeTraceCount("gangnam-1", 9L)
        ));
        when(boardRepository.findByKakaoPlaceIdIn(anyCollection())).thenReturn(List.of());

        List<PopularPlaceResponse> responses = placeService.getPopularPlaces(10, " ");

        assertThat(responses).extracting(PopularPlaceResponse::getKakaoPlaceId)
                .containsExactly("seongdong-1", "gangnam-1");
    }

    private PlaceService placeServiceWithCache(String csv) throws Exception {
        Path cacheFile = tempDir.resolve("places-cache.csv");
        Files.writeString(cacheFile, csv);
        PlaceCsvStore placeCsvStore = new PlaceCsvStore(cacheFile.toString());
        return new PlaceService(
                kakaoLocalService,
                boardRepository,
                traceRepository,
                placeCsvStore,
                new PlaceSearchRequestValidator()
        );
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
