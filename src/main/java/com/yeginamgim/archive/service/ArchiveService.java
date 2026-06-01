package com.yeginamgim.archive.service;

import com.yeginamgim.archive.dto.ArchiveBoardGroupResponse;
import com.yeginamgim.archive.dto.ArchiveBoardListResponse;
import com.yeginamgim.archive.dto.ArchiveCalendarResponse;
import com.yeginamgim.archive.dto.ArchiveDateGroupResponse;
import com.yeginamgim.archive.dto.ArchiveTraceListResponse;
import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.board.dto.PlaceInfo;
import com.yeginamgim.board.entity.BoardEntity;
import com.yeginamgim.board.service.BoardService;
import com.yeginamgim.trace.dto.TraceElementResponse;
import com.yeginamgim.trace.dto.TraceResponse;
import com.yeginamgim.trace.entity.Trace;
import com.yeginamgim.trace.entity.TraceElement;
import com.yeginamgim.trace.enums.TraceStatus;
import com.yeginamgim.trace.repository.TraceElementRepository;
import com.yeginamgim.trace.repository.TraceLikeRepository;
import com.yeginamgim.trace.repository.TraceRepository;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArchiveService {

    private final TraceRepository traceRepository;
    private final TraceElementRepository traceElementRepository;
    private final TraceLikeRepository traceLikeRepository;
    private final UserRepository userRepository;
    private final BoardService boardService;
    private final JWTService jwtService;

    // 내가 남긴 흔적 전체 조회
    @Transactional(readOnly = true)
    public ArchiveTraceListResponse getMyTraces(String authorization) {
        Long userId = findUserByToken(authorization).getUserId();

        List<Trace> traces = traceRepository
                .findByUser_UserIdAndTraceStatusOrderByCreatedAtDescTraceIdDesc(userId, TraceStatus.ACTIVE);

        return ArchiveTraceListResponse.builder()
                .userId(userId)
                .traces(toTraceResponses(traces))
                .build();
    }

    // 내가 남긴 흔적 개별 조회
    @Transactional(readOnly = true)
    public TraceResponse getMyTrace(String authorization, Long traceId) {
        Long userId = findUserByToken(authorization).getUserId();

        Trace trace = traceRepository
                .findByTraceIdAndUser_UserIdAndTraceStatus(traceId, userId, TraceStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "내가 남긴 흔적을 찾을 수 없습니다."));

        List<TraceElement> elements = traceElementRepository.findByTrace_TraceIdOrderByElementIdAsc(trace.getTraceId());

        return toTraceResponse(trace, elements);
    }

    // 날짜별 기록 조회
    @Transactional(readOnly = true)
    public ArchiveCalendarResponse getCalendar(String authorization, int year, int month) {
        Long userId = findUserByToken(authorization).getUserId();
        validateMonth(month);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startAt = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endAt = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<Trace> traces = traceRepository
                .findByUser_UserIdAndTraceStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDescTraceIdDesc(
                        userId,
                        TraceStatus.ACTIVE,
                        startAt,
                        endAt
                );

        Map<LocalDate, List<TraceResponse>> tracesByDate = toTraceResponses(traces).stream()
                .collect(Collectors.groupingBy(
                        trace -> trace.getCreatedAt().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<ArchiveDateGroupResponse> days = tracesByDate.entrySet().stream()
                .map(entry -> ArchiveDateGroupResponse.builder()
                        .date(entry.getKey())
                        .traceCount(entry.getValue().size())
                        .traces(entry.getValue())
                        .build())
                .toList();

        return ArchiveCalendarResponse.builder()
                .userId(userId)
                .year(year)
                .month(month)
                .days(days)
                .build();
    }

    // 공간별 추억 아카이브 조회
    @Transactional(readOnly = true)
    public ArchiveBoardListResponse getBoardArchives(String authorization) {
        Long userId = findUserByToken(authorization).getUserId();

        List<Trace> traces = traceRepository
                .findByUser_UserIdAndTraceStatusOrderByCreatedAtDescTraceIdDesc(userId, TraceStatus.ACTIVE);

        Map<Long, TraceResponse> traceResponseMap = toTraceResponses(traces).stream()
                .collect(Collectors.toMap(
                        TraceResponse::getTraceId,
                        Function.identity(),
                        (first, second) -> first,
                        LinkedHashMap::new
                ));

        Map<Long, List<Trace>> tracesByBoard = traces.stream()
                .collect(Collectors.groupingBy(
                        trace -> trace.getBoard().getBoardId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<ArchiveBoardGroupResponse> boards = tracesByBoard.values().stream()
                .map(boardTraces -> {
                    BoardEntity board = boardTraces.get(0).getBoard();
                    PlaceInfo place = boardService.getPlaceInfoByKakaoPlaceId(board.getKakaoPlaceId());
                    List<TraceResponse> boardTraceResponses = boardTraces.stream()
                            .map(trace -> traceResponseMap.get(trace.getTraceId()))
                            .toList();

                    return ArchiveBoardGroupResponse.builder()
                            .boardId(board.getBoardId())
                            .kakaoPlaceId(board.getKakaoPlaceId())
                            .placeName(place.getPlaceName())
                            .groupName(place.getGroupName())
                            .traceCount(boardTraceResponses.size())
                            .traces(boardTraceResponses)
                            .build();
                })
                .toList();

        return ArchiveBoardListResponse.builder()
                .userId(userId)
                .boards(boards)
                .build();
    }

    // 내가 작성한 흔적 중 좋아요를 받은 흔적 조회
    @Transactional(readOnly = true)
    public ArchiveTraceListResponse getReceivedLikeTraces(String authorization) {
        Long userId = findUserByToken(authorization).getUserId();

        List<Trace> traces = traceRepository
                .findByUser_UserIdAndTraceStatusOrderByCreatedAtDescTraceIdDesc(userId, TraceStatus.ACTIVE);

        List<TraceResponse> likedTraces = toTraceResponses(traces).stream()
                .filter(trace -> trace.getLikeCount() > 0)
                .toList();

        return ArchiveTraceListResponse.builder()
                .userId(userId)
                .traces(likedTraces)
                .build();
    }

    private UserEntity findUserByToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 요청입니다.");
        }

        String email = jwtService.getClaim(authorization);
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 요청입니다.");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private void validateMonth(int month) {
        if (month < 1 || month > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "month는 1부터 12 사이여야 합니다.");
        }
    }

    private List<TraceResponse> toTraceResponses(List<Trace> traces) {
        List<Long> traceIds = traces.stream()
                .map(Trace::getTraceId)
                .toList();

        Map<Long, List<TraceElement>> elementMap = traceIds.isEmpty()
                ? Map.of()
                : traceElementRepository
                        .findByTrace_TraceIdInOrderByElementIdAsc(traceIds)
                        .stream()
                        .collect(Collectors.groupingBy(element -> element.getTrace().getTraceId()));

        return traces.stream()
                .map(trace -> toTraceResponse(
                        trace,
                        elementMap.getOrDefault(trace.getTraceId(), List.of())
                ))
                .toList();
    }

    private TraceResponse toTraceResponse(Trace trace, List<TraceElement> elements) {
        return TraceResponse.builder()
                .traceId(trace.getTraceId())
                .boardId(trace.getBoard().getBoardId())
                .userId(trace.getUser().getUserId())
                .nickname(trace.getUser().getNickname())
                .traceX(trace.getTraceX())
                .traceY(trace.getTraceY())
                .traceStatus(trace.getTraceStatus().name())
                .createdAt(trace.getCreatedAt())
                .updatedAt(trace.getUpdatedAt())
                .likeCount(traceLikeRepository.countByTrace_TraceId(trace.getTraceId()))
                .elements(elements.stream()
                        .map(this::toTraceElementResponse)
                        .toList())
                .build();
    }

    private TraceElementResponse toTraceElementResponse(TraceElement element) {
        return TraceElementResponse.builder()
                .elementId(element.getElementId())
                .contentType(element.getContentType().name())
                .textContent(element.getTextContent())
                .imageUrl(element.getImageUrl())
                .elementX(element.getElementX())
                .elementY(element.getElementY())
                .styleJson(element.getStyleJson())
                .createdAt(element.getCreatedAt())
                .updatedAt(element.getUpdatedAt())
                .build();
    }
}
