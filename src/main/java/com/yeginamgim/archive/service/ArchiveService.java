package com.yeginamgim.archive.service;

import com.yeginamgim.archive.dto.ArchiveBoardGroupResponse;
import com.yeginamgim.archive.dto.ArchiveBoardListResponse;
import com.yeginamgim.archive.dto.ArchiveCalendarResponse;
import com.yeginamgim.archive.dto.ArchiveDateGroupResponse;
import com.yeginamgim.archive.dto.ArchiveTraceListResponse;
import com.yeginamgim.archive.dto.FavoritePlaceListResponse;
import com.yeginamgim.archive.dto.FavoritePlaceRequest;
import com.yeginamgim.archive.dto.FavoritePlaceResponse;
import com.yeginamgim.archive.entity.FavoritePlace;
import com.yeginamgim.archive.repository.FavoritePlaceRepository;
import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.board.dto.PlaceInfo;
import com.yeginamgim.board.entity.BoardEntity;
import com.yeginamgim.board.repository.BoardRepository;
import com.yeginamgim.board.service.BoardService;
import com.yeginamgim.place.repository.PlaceCsvStore;
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
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArchiveService {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

        private final TraceRepository traceRepository;
    private final TraceElementRepository traceElementRepository;
    private final TraceLikeRepository traceLikeRepository;
    private final FavoritePlaceRepository favoritePlaceRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardService boardService;
    private final PlaceCsvStore placeCsvStore;
    private final JWTService jwtService;

    // 내가 남긴 흔적 전체 조회
    @Transactional(readOnly = true)
    public ArchiveTraceListResponse getMyTraces(String authorization) {
        Long userId = findUserByToken(authorization).getUserId();

        List<Trace> traces = traceRepository
                .findByUser_UserIdAndTraceStatusOrderByCreatedAtDescTraceIdDesc(userId, TraceStatus.ACTIVE);

        return ArchiveTraceListResponse.of(userId, toTraceResponses(traces, userId));
    }

    // 내가 남긴 흔적 개별 조회
    @Transactional(readOnly = true)
    public TraceResponse getMyTrace(String authorization, Long traceId) {
        Long userId = findUserByToken(authorization).getUserId();

        Trace trace = traceRepository
                .findByTraceIdAndUser_UserIdAndTraceStatus(traceId, userId, TraceStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "내가 남긴 흔적을 찾을 수 없습니다."));

        List<TraceElement> elements = traceElementRepository.findByTrace_TraceIdOrderByElementIdAsc(trace.getTraceId());

        return toTraceResponse(trace, elements, userId);
    }

    // 날짜별 기록 조회
    @Transactional(readOnly = true)
    public ArchiveCalendarResponse getCalendar(String authorization, int year, int month) {
        Long userId = findUserByToken(authorization).getUserId();
        validateMonth(month);

        YearMonth yearMonth = YearMonth.of(year, month);
        Instant startAt = yearMonth.atDay(1).atStartOfDay(SEOUL_ZONE).toInstant();
        Instant endAt = yearMonth.plusMonths(1).atDay(1).atStartOfDay(SEOUL_ZONE).toInstant();

        List<Trace> traces = traceRepository
                .findByUser_UserIdAndTraceStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDescTraceIdDesc(
                        userId,
                        TraceStatus.ACTIVE,
                        startAt,
                        endAt
                );

        Map<LocalDate, List<TraceResponse>> tracesByDate = toTraceResponses(traces, userId).stream()
                .collect(Collectors.groupingBy(
                        trace -> trace.getCreatedAt().atZone(SEOUL_ZONE).toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<ArchiveDateGroupResponse> days = tracesByDate.entrySet().stream()
                .map(entry -> ArchiveDateGroupResponse.of(entry.getKey(), entry.getValue()))
                .toList();

        return ArchiveCalendarResponse.of(userId, year, month, days);
    }

    // 공간별 추억 아카이브 조회
    @Transactional(readOnly = true)
    public ArchiveBoardListResponse getBoardArchives(String authorization) {
        Long userId = findUserByToken(authorization).getUserId();

        List<Trace> traces = traceRepository
                .findByUser_UserIdAndTraceStatusOrderByCreatedAtDescTraceIdDesc(userId, TraceStatus.ACTIVE);
        List<Trace> placeBoardTraces = traces.stream()
                .filter(trace -> trace.getBoard() != null)
                .toList();

        Map<Long, TraceResponse> traceResponseMap = toTraceResponses(placeBoardTraces, userId).stream()
                .collect(Collectors.toMap(
                        TraceResponse::getTraceId,
                        Function.identity(),
                        (first, second) -> first,
                        LinkedHashMap::new
                ));

        Map<Long, List<Trace>> tracesByBoard = placeBoardTraces.stream()
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

                    return ArchiveBoardGroupResponse.from(board, place, boardTraceResponses);
                })
                .toList();

        return ArchiveBoardListResponse.of(userId, boards);
    }

    // 내가 작성한 흔적 중 좋아요를 받은 흔적 조회
    @Transactional(readOnly = true)
    public ArchiveTraceListResponse getReceivedLikeTraces(String authorization) {
        Long userId = findUserByToken(authorization).getUserId();

        List<Trace> traces = traceRepository
                .findByUser_UserIdAndTraceStatusOrderByCreatedAtDescTraceIdDesc(userId, TraceStatus.ACTIVE);

        List<TraceResponse> likedTraces = toTraceResponses(traces, userId).stream()
                .filter(trace -> trace.getLikeCount() > 0)
                .toList();

        return ArchiveTraceListResponse.of(userId, likedTraces);
    }

    // 내가 즐겨찾기한 장소 목록 조회
    @Transactional(readOnly = true)
    public FavoritePlaceListResponse getFavoritePlaces(String authorization) {
        Long userId = findUserByToken(authorization).getUserId();

        List<FavoritePlaceResponse> places = favoritePlaceRepository
                .findByUser_UserIdOrderByCreatedAtDescFavoritePlaceIdDesc(userId)
                .stream()
                .map(this::toFavoritePlaceResponse)
                .toList();

        return FavoritePlaceListResponse.of(userId, places);
    }

    // 장소 즐겨찾기 등록
    @Transactional
    public FavoritePlaceResponse addFavoritePlace(
            String authorization,
            String kakaoPlaceId,
            FavoritePlaceRequest request
    ) {
        UserEntity user = findUserByToken(authorization);
        validateKakaoPlaceId(kakaoPlaceId);
        savePlaceSnapshotIfNeeded(kakaoPlaceId, request);

        FavoritePlace favoritePlace = favoritePlaceRepository
                .findByUser_UserIdAndKakaoPlaceId(user.getUserId(), kakaoPlaceId)
                .orElseGet(() -> favoritePlaceRepository.save(FavoritePlace.create(user, kakaoPlaceId)));

        return toFavoritePlaceResponse(favoritePlace);
    }

    // 장소 즐겨찾기 취소
    @Transactional
    public void removeFavoritePlace(String authorization, String kakaoPlaceId) {
        Long userId = findUserByToken(authorization).getUserId();
        validateKakaoPlaceId(kakaoPlaceId);

        if (favoritePlaceRepository.existsByUser_UserIdAndKakaoPlaceId(userId, kakaoPlaceId)) {
            favoritePlaceRepository.deleteByUser_UserIdAndKakaoPlaceId(userId, kakaoPlaceId);
        }
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

    private void validateKakaoPlaceId(String kakaoPlaceId) {
        if (!StringUtils.hasText(kakaoPlaceId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "kakaoPlaceId는 필수입니다.");
        }
    }

    private void savePlaceSnapshotIfNeeded(String kakaoPlaceId, FavoritePlaceRequest request) {
        if (placeCsvStore.findByKakaoPlaceId(kakaoPlaceId).isPresent()) {
            return;
        }

        if (!hasPlaceSnapshot(request)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "즐겨찾기할 장소 정보를 찾을 수 없습니다.");
        }

        placeCsvStore.saveIfAbsent(PlaceInfo.builder()
                .kakaoPlaceId(kakaoPlaceId)
                .placeName(request.getPlaceName())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .phone(request.getPhone())
                .address(request.getAddress())
                .kakaoMapUrl(request.getKakaoMapUrl())
                .groupName(request.getGroupName())
                .build());
    }

    private boolean hasPlaceSnapshot(FavoritePlaceRequest request) {
        return request != null
                && StringUtils.hasText(request.getPlaceName())
                && request.getLatitude() != null
                && request.getLongitude() != null
                && StringUtils.hasText(request.getGroupName());
    }

    private List<TraceResponse> toTraceResponses(List<Trace> traces, Long viewerUserId) {
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
                        elementMap.getOrDefault(trace.getTraceId(), List.of()),
                        viewerUserId
                ))
                .toList();
    }

    private TraceResponse toTraceResponse(Trace trace, List<TraceElement> elements, Long viewerUserId) {
        List<TraceElementResponse> elementResponses = elements.stream()
                .map(TraceElementResponse::from)
                .toList();

        return TraceResponse.from(
                trace,
                elementResponses,
                traceLikeRepository.countByTrace_TraceId(trace.getTraceId()),
                isLikedByUser(viewerUserId, trace.getTraceId())
        );
    }

    private boolean isLikedByUser(Long userId, Long traceId) {
        return userId != null && traceLikeRepository.existsByUser_UserIdAndTrace_TraceId(userId, traceId);
    }

    private FavoritePlaceResponse toFavoritePlaceResponse(FavoritePlace favoritePlace) {
        PlaceInfo placeInfo = placeCsvStore.findByKakaoPlaceId(favoritePlace.getKakaoPlaceId())
                .orElseGet(() -> PlaceInfo.builder()
                        .kakaoPlaceId(favoritePlace.getKakaoPlaceId())
                        .build());
        Long boardId = boardRepository.findByKakaoPlaceId(favoritePlace.getKakaoPlaceId())
                .map(BoardEntity::getBoardId)
                .orElse(null);

        return FavoritePlaceResponse.from(favoritePlace, placeInfo, boardId);
    }
}
