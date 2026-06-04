package com.yeginamgim.board.service;

import com.yeginamgim.board.dto.BoardCreateRequest;
import com.yeginamgim.board.dto.BoardDetailResponse;
import com.yeginamgim.board.dto.PlaceInfo;
import com.yeginamgim.board.entity.BoardEntity;
import com.yeginamgim.board.repository.BoardRepository;
import com.yeginamgim.place.repository.PlaceCsvStore;
import com.yeginamgim.place.service.PlaceService;
import com.yeginamgim.trace.repository.TraceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BoardServiceTest {

    @TempDir
    Path tempDir;

    private final BoardRepository boardRepository = mock(BoardRepository.class);
    private final TraceRepository traceRepository = mock(TraceRepository.class);

    @Test
    void boardDetailIncludesActiveTraceCount() throws Exception {
        Path cacheFile = emptyCacheFile();
        PlaceService placeService = mock(PlaceService.class);
        BoardEntity board = BoardEntity.builder()
                .boardId(11L)
                .kakaoPlaceId("new-place")
                .createdAt(LocalDateTime.now())
                .build();
        when(boardRepository.findById(11L)).thenReturn(Optional.of(board));
        when(placeService.findPlaceInfoByKakaoPlaceId("new-place")).thenReturn(PlaceInfo.builder()
                .kakaoPlaceId("new-place")
                .placeName("New Cafe")
                .latitude(37.4979)
                .longitude(127.0276)
                .groupName("cafe")
                .build());
        when(traceRepository.countActiveByBoardId(11L)).thenReturn(12L);

        BoardService boardService = new BoardService(
                boardRepository,
                placeService,
                new PlaceCsvStore(cacheFile.toString()),
                traceRepository
        );

        BoardDetailResponse response = boardService.getBoardDetail(11L);

        assertThat(response.getTraceCount()).isEqualTo(12L);
    }

    @Test
    void createsBoardAndCachesPlaceSnapshotWhenPlaceIsNotCached() throws Exception {
        Path cacheFile = emptyCacheFile();
        PlaceService placeService = mock(PlaceService.class);
        when(boardRepository.findByKakaoPlaceId("new-place")).thenReturn(Optional.empty());
        when(boardRepository.save(any(BoardEntity.class))).thenAnswer(invocation -> {
            BoardEntity board = invocation.getArgument(0);
            board.setBoardId(11L);
            board.setCreatedAt(LocalDateTime.now());
            return board;
        });
        when(placeService.findPlaceInfoByKakaoPlaceId("new-place")).thenAnswer(invocation ->
                new PlaceCsvStore(cacheFile.toString()).findByKakaoPlaceId("new-place").orElseThrow());

        BoardService boardService = new BoardService(
                boardRepository,
                placeService,
                new PlaceCsvStore(cacheFile.toString()),
                traceRepository
        );

        boardService.createBoard(BoardCreateRequest.builder()
                .kakaoPlaceId("new-place")
                .placeName("New Cafe")
                .latitude(37.4979)
                .longitude(127.0276)
                .phone("02-0000-0000")
                .address("Seoul")
                .kakaoMapUrl("https://place.map.kakao.com/new-place")
                .groupName("cafe")
                .build());

        assertThat(Files.readString(cacheFile)).contains("new-place,New Cafe");
    }

    @Test
    void rejectsUncachedPlaceWithoutSnapshot() throws Exception {
        Path cacheFile = emptyCacheFile();
        PlaceService placeService = mock(PlaceService.class);
        when(placeService.findPlaceInfoByKakaoPlaceId("missing")).thenThrow(ResponseStatusException.class);
        BoardService boardService = new BoardService(
                boardRepository,
                placeService,
                new PlaceCsvStore(cacheFile.toString()),
                traceRepository
        );

        assertThatThrownBy(() -> boardService.createBoard(BoardCreateRequest.builder()
                .kakaoPlaceId("missing")
                .build()))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void doesNotDuplicateUncachedSnapshotWithSameNameCoordinatesAndAddress() throws Exception {
        Path cacheFile = tempDir.resolve("places-cache.csv");
        Files.writeString(cacheFile, """
                kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name
                ,Same Cafe,37.4979,127.0276,02-0000-0000,Seoul,https://place.map.kakao.com/old,cafe
                """);
        PlaceService placeService = mock(PlaceService.class);
        when(boardRepository.findByKakaoPlaceId("new-id")).thenReturn(Optional.empty());
        when(boardRepository.save(any(BoardEntity.class))).thenAnswer(invocation -> {
            BoardEntity board = invocation.getArgument(0);
            board.setBoardId(11L);
            board.setCreatedAt(LocalDateTime.now());
            return board;
        });
        when(placeService.findPlaceInfoByKakaoPlaceId("new-id")).thenAnswer(invocation ->
                new PlaceCsvStore(cacheFile.toString()).findByKakaoPlaceId("new-id").orElseThrow());

        BoardService boardService = new BoardService(
                boardRepository,
                placeService,
                new PlaceCsvStore(cacheFile.toString()),
                traceRepository
        );

        boardService.createBoard(BoardCreateRequest.builder()
                .kakaoPlaceId("new-id")
                .placeName("Same Cafe")
                .latitude(37.4979)
                .longitude(127.0276)
                .phone("02-1111-1111")
                .address("Seoul")
                .kakaoMapUrl("https://place.map.kakao.com/new-id")
                .groupName("cafe")
                .build());

        assertThat(Files.readString(cacheFile).lines()
                .filter(line -> line.contains("Same Cafe"))
                .count()).isEqualTo(1);
    }

    private Path emptyCacheFile() throws Exception {
        Path cacheFile = tempDir.resolve("places-cache.csv");
        Files.writeString(cacheFile, "kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name\n");
        return cacheFile;
    }
}
