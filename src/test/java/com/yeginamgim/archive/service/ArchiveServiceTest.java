package com.yeginamgim.archive.service;

import com.yeginamgim.archive.repository.FavoritePlaceRepository;
import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.board.dto.PlaceInfo;
import com.yeginamgim.board.entity.BoardEntity;
import com.yeginamgim.board.repository.BoardRepository;
import com.yeginamgim.board.service.BoardService;
import com.yeginamgim.customboard.entity.CustomBoard;
import com.yeginamgim.place.repository.PlaceCsvStore;
import com.yeginamgim.trace.entity.Trace;
import com.yeginamgim.trace.enums.TraceStatus;
import com.yeginamgim.trace.repository.TraceElementRepository;
import com.yeginamgim.trace.repository.TraceLikeRepository;
import com.yeginamgim.trace.repository.TraceRepository;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArchiveServiceTest {

    private final TraceRepository traceRepository = mock(TraceRepository.class);
    private final TraceElementRepository traceElementRepository = mock(TraceElementRepository.class);
    private final TraceLikeRepository traceLikeRepository = mock(TraceLikeRepository.class);
    private final FavoritePlaceRepository favoritePlaceRepository = mock(FavoritePlaceRepository.class);
    private final BoardRepository boardRepository = mock(BoardRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final BoardService boardService = mock(BoardService.class);
    private final PlaceCsvStore placeCsvStore = mock(PlaceCsvStore.class);
    private final JWTService jwtService = mock(JWTService.class);

    private final ArchiveService archiveService = new ArchiveService(
            traceRepository,
            traceElementRepository,
            traceLikeRepository,
            favoritePlaceRepository,
            boardRepository,
            userRepository,
            boardService,
            placeCsvStore,
            jwtService
    );

    @Test
    void boardArchivesIgnoreCustomBoardTraces() {
        UserEntity user = UserEntity.builder()
                .userId(1L)
                .email("user@example.com")
                .nickname("user")
                .build();
        BoardEntity placeBoard = BoardEntity.builder()
                .boardId(3L)
                .kakaoPlaceId("place-1")
                .build();
        CustomBoard customBoard = CustomBoard.builder()
                .customBoardId(9L)
                .user(user)
                .boardTitle("custom")
                .build();
        Trace placeTrace = Trace.builder()
                .traceId(10L)
                .user(user)
                .board(placeBoard)
                .traceX(1)
                .traceY(2)
                .traceStatus(TraceStatus.ACTIVE)
                .build();
        Trace customTrace = Trace.builder()
                .traceId(11L)
                .user(user)
                .customBoard(customBoard)
                .traceX(3)
                .traceY(4)
                .traceStatus(TraceStatus.ACTIVE)
                .build();

        when(jwtService.getClaim("Bearer token")).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(traceRepository.findByUser_UserIdAndTraceStatusOrderByCreatedAtDescTraceIdDesc(1L, TraceStatus.ACTIVE))
                .thenReturn(List.of(customTrace, placeTrace));
        when(traceElementRepository.findByTrace_TraceIdInOrderByElementIdAsc(List.of(11L, 10L)))
                .thenReturn(List.of());
        when(boardService.getPlaceInfoByKakaoPlaceId("place-1")).thenReturn(PlaceInfo.builder()
                .kakaoPlaceId("place-1")
                .placeName("place")
                .groupName("group")
                .build());

        var response = archiveService.getBoardArchives("Bearer token");

        assertThat(response.getBoards()).hasSize(1);
        assertThat(response.getBoards().get(0).getBoardId()).isEqualTo(3L);
        assertThat(response.getBoards().get(0).getTraces()).extracting("traceId").containsExactly(10L);
    }
}
