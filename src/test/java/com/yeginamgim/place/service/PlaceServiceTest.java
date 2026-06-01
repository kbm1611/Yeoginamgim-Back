package com.yeginamgim.place.service;

import com.yeginamgim.board.entity.BoardEntity;
import com.yeginamgim.board.repository.BoardRepository;
import com.yeginamgim.place.dto.response.PlaceResponse;
import com.yeginamgim.place.dto.response.PopularPlaceResponse;
import com.yeginamgim.trace.repository.TraceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlaceServiceTest {

    private final KakaoLocalService kakaoLocalService = mock(KakaoLocalService.class);
    private final BoardRepository boardRepository = mock(BoardRepository.class);
    private final TraceRepository traceRepository = mock(TraceRepository.class);
    private final PlaceService placeService = new PlaceService(kakaoLocalService, boardRepository, traceRepository);

    @Test
    void findsPlaceFromCsvAndAddsBoardAndTraceCount() {
        BoardEntity board = BoardEntity.builder()
                .boardId(1L)
                .kakaoPlaceId("26338954")
                .createdAt(LocalDateTime.now())
                .build();
        when(boardRepository.findByKakaoPlaceId("26338954")).thenReturn(Optional.of(board));
        when(traceRepository.countActiveByKakaoPlaceId("26338954")).thenReturn(3L);

        PlaceResponse response = placeService.getPlaceByKakaoPlaceId("26338954");

        assertThat(response.getKakaoPlaceId()).isEqualTo("26338954");
        assertThat(response.getPlaceName()).isEqualTo("Sample Starbucks Gangnam");
        assertThat(response.getLatitude()).isEqualTo(37.4979);
        assertThat(response.getLongitude()).isEqualTo(127.0276);
        assertThat(response.getBoardId()).isEqualTo(1L);
        assertThat(response.getTraceCount()).isEqualTo(3L);
    }

    @Test
    void throwsNotFoundWhenPlaceDoesNotExistInAnySource() {
        when(kakaoLocalService.findByKakaoPlaceId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> placeService.getPlaceByKakaoPlaceId("missing"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    void returnsPopularPlacesOrderedByActiveTraceCount() {
        when(traceRepository.countActiveTracesByPlace()).thenReturn(List.of(
                placeTraceCount("26338954", 5L),
                placeTraceCount("73753737", 2L)
        ));

        List<PopularPlaceResponse> responses = placeService.getPopularPlaces(10);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getRank()).isEqualTo(1);
        assertThat(responses.get(0).getKakaoPlaceId()).isEqualTo("26338954");
        assertThat(responses.get(0).getTraceCount()).isEqualTo(5L);
        assertThat(responses.get(1).getRank()).isEqualTo(2);
        assertThat(responses.get(1).getKakaoPlaceId()).isEqualTo("73753737");
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
