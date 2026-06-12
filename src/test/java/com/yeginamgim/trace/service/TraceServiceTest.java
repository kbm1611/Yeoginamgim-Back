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
import com.yeginamgim.trace.dto.TraceElementCreateRequest;
import com.yeginamgim.trace.dto.TraceElementUpdateRequest;
import com.yeginamgim.trace.dto.TraceUpdateRequest;
import com.yeginamgim.trace.entity.Trace;
import com.yeginamgim.trace.entity.TraceElement;
import com.yeginamgim.trace.enums.ContentType;
import com.yeginamgim.trace.enums.TraceStatus;
import com.yeginamgim.trace.repository.TraceElementRepository;
import com.yeginamgim.trace.repository.TraceLikeRepository;
import com.yeginamgim.trace.repository.TraceRepository;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    private final ProfanityFilterService profanityFilterService = mock(ProfanityFilterService.class);
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
            notificationService,
            profanityFilterService
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

    @Test
    void createTraceWithoutTextContentSkipsProfanityFilter() {
        UserEntity user = user();
        BoardEntity board = board();
        TraceCreateRequest request = createRequest();
        TraceElementCreateRequest imageOnly = new TraceElementCreateRequest();
        imageOnly.setContentType(ContentType.POLAROID);
        imageOnly.setImageUrl("/upload/board/photo.png");
        request.setElements(List.of(imageOnly));

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

        verifyNoInteractions(profanityFilterService);
    }

    @Test
    void createTraceValidatesOnlyNonBlankTextContentBeforeSaving() {
        UserEntity user = user();
        BoardEntity board = board();
        TraceCreateRequest request = createRequest();
        TraceElementCreateRequest text = new TraceElementCreateRequest();
        text.setContentType(ContentType.POST_IT);
        text.setTextContent("  좋은 기억  ");
        TraceElementCreateRequest blank = new TraceElementCreateRequest();
        blank.setContentType(ContentType.POST_IT);
        blank.setTextContent("   ");
        request.setElements(List.of(text, blank));

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

        verify(profanityFilterService).validateTexts(List.of("좋은 기억"));
        verify(traceRepository).save(any(Trace.class));
    }

    @Test
    void updateTraceRejectsBlockedTextBeforeApplyingChanges() {
        UserEntity user = user();
        Trace trace = Trace.builder()
                .traceId(10L)
                .user(user)
                .board(board())
                .traceX(10)
                .traceY(20)
                .traceStatus(TraceStatus.ACTIVE)
                .build();
        TraceElement element = TraceElement.builder()
                .trace(trace)
                .contentType(ContentType.POST_IT)
                .textContent("기존")
                .elementX(1)
                .elementY(2)
                .build();
        element.setElementId(100L);
        TraceUpdateRequest request = new TraceUpdateRequest();
        request.setTraceX(99);
        TraceElementUpdateRequest elementRequest = new TraceElementUpdateRequest();
        elementRequest.setElementId(100L);
        elementRequest.setContentType(ContentType.POST_IT);
        elementRequest.setTextContent("나쁜 말");
        request.setElements(List.of(elementRequest));

        when(jwtService.getClaim("Bearer token")).thenReturn("writer@example.com");
        when(userRepository.findByEmail("writer@example.com")).thenReturn(Optional.of(user));
        when(traceRepository.findByTraceIdAndUser_UserIdAndTraceStatus(10L, 1L, TraceStatus.ACTIVE))
                .thenReturn(Optional.of(trace));
        when(traceElementRepository.findByTrace_TraceIdOrderByElementIdAsc(10L)).thenReturn(List.of(element));
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "부적절한 표현이 포함되어 있습니다."))
                .when(profanityFilterService).validateTexts(List.of("나쁜 말"));

        assertThatThrownBy(() -> traceService.updateTrace(10L, "Bearer token", request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("부적절한 표현이 포함되어 있습니다.");

        org.assertj.core.api.Assertions.assertThat(trace.getTraceX()).isEqualTo(10);
        org.assertj.core.api.Assertions.assertThat(element.getTextContent()).isEqualTo("기존");
        verify(traceElementRepository, never()).saveAll(any());
    }

    private UserEntity user() {
        return UserEntity.builder()
                .userId(1L)
                .email("writer@example.com")
                .nickname("writer")
                .build();
    }

    private BoardEntity board() {
        return BoardEntity.builder()
                .boardId(3L)
                .kakaoPlaceId("place-1")
                .build();
    }

    private TraceCreateRequest createRequest() {
        TraceCreateRequest request = new TraceCreateRequest();
        request.setTraceX(30);
        request.setTraceY(50);
        return request;
    }
}
