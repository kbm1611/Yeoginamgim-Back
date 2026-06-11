package com.yeginamgim.trace.service;

import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.board.entity.BoardEntity;
import com.yeginamgim.board.repository.BoardRepository;
import com.yeginamgim.customboard.repository.CustomBoardMemberRepository;
import com.yeginamgim.customboard.repository.CustomBoardRepository;
import com.yeginamgim.global.file.FileService;
import com.yeginamgim.notification.service.NotificationService;
import com.yeginamgim.place.repository.PlaceCsvStore;
import com.yeginamgim.trace.dto.TraceCreateRequest;
import com.yeginamgim.trace.entity.Trace;
import com.yeginamgim.trace.repository.TraceElementRepository;
import com.yeginamgim.trace.repository.TraceLikeRepository;
import com.yeginamgim.trace.repository.TraceRepository;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TraceServiceTest {

    private final TraceRepository traceRepository = mock(TraceRepository.class);
    private final TraceElementRepository traceElementRepository = mock(TraceElementRepository.class);
    private final TraceLikeRepository traceLikeRepository = mock(TraceLikeRepository.class);
    private final BoardRepository boardRepository = mock(BoardRepository.class);
    private final CustomBoardRepository customBoardRepository = mock(CustomBoardRepository.class);
    private final CustomBoardMemberRepository customBoardMemberRepository = mock(CustomBoardMemberRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final FileService fileService = mock(FileService.class);
    private final JWTService jwtService = mock(JWTService.class);
    private final PlaceCsvStore placeCsvStore = mock(PlaceCsvStore.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final TraceService traceService = new TraceService(
            traceRepository,
            traceElementRepository,
            traceLikeRepository,
            boardRepository,
            customBoardRepository,
            customBoardMemberRepository,
            userRepository,
            fileService,
            jwtService,
            placeCsvStore,
            notificationService
    );

    @Test
    void createTraceNotifiesFollowersAfterTraceSaved() {
        UserEntity user = UserEntity.builder()
                .userId(1L)
                .email("writer@example.com")
                .nickname("writer")
                .build();
        BoardEntity board = BoardEntity.builder()
                .boardId(3L)
                .kakaoPlaceId("place-1")
                .build();
        TraceCreateRequest request = new TraceCreateRequest();
        request.setTraceX(30);
        request.setTraceY(50);

        when(boardRepository.findById(3L)).thenReturn(Optional.of(board));
        when(jwtService.getClaim("Bearer token")).thenReturn("writer@example.com");
        when(userRepository.findByEmail("writer@example.com")).thenReturn(Optional.of(user));
        when(traceRepository.save(any(Trace.class))).thenAnswer(invocation -> {
            Trace trace = invocation.getArgument(0);
            trace.setTraceId(10L);
            return trace;
        });
        when(traceLikeRepository.countByTrace_TraceId(10L)).thenReturn(0L);

        traceService.createTrace(3L, "Bearer token", request);

        ArgumentCaptor<Trace> traceCaptor = ArgumentCaptor.forClass(Trace.class);
        verify(notificationService).createFollowingTraceNotifications(eq(user), traceCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(traceCaptor.getValue().getTraceId()).isEqualTo(10L);
    }
}
