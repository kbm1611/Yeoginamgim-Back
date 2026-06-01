package com.yeginamgim.board.service;

import com.yeginamgim.board.dto.BoardCreateRequest;
import com.yeginamgim.board.entity.BoardEntity;
import com.yeginamgim.board.repository.BoardRepository;
import com.yeginamgim.place.service.PlaceCsvStore;
import com.yeginamgim.place.service.PlaceService;
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

        BoardService boardService = new BoardService(boardRepository, placeService, new PlaceCsvStore(cacheFile.toString()));

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
        BoardService boardService = new BoardService(boardRepository, placeService, new PlaceCsvStore(cacheFile.toString()));

        assertThatThrownBy(() -> boardService.createBoard(BoardCreateRequest.builder()
                .kakaoPlaceId("missing")
                .build()))
                .isInstanceOf(ResponseStatusException.class);
    }

    private Path emptyCacheFile() throws Exception {
        Path cacheFile = tempDir.resolve("places-cache.csv");
        Files.writeString(cacheFile, "kakao_place_id,place_name,latitude,longitude,phone,address,kakao_map_url,group_name\n");
        return cacheFile;
    }
}
