package com.yeginamgim.trace.service;

import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.board.dto.PlaceInfo;
import com.yeginamgim.board.entity.BoardEntity;
import com.yeginamgim.board.repository.BoardRepository;
import com.yeginamgim.customboard.entity.CustomBoard;
import com.yeginamgim.customboard.enums.BoardRole;
import com.yeginamgim.customboard.repository.CustomBoardMemberRepository;
import com.yeginamgim.customboard.repository.CustomBoardRepository;
import com.yeginamgim.global.file.FileService;
import com.yeginamgim.global.util.PeriodRange;
import com.yeginamgim.notification.service.NotificationService;
import com.yeginamgim.place.repository.PlaceCsvStore;
import com.yeginamgim.trace.dto.RecentTraceResponse;
import com.yeginamgim.trace.dto.TraceCreateRequest;
import com.yeginamgim.trace.dto.TraceElementCreateRequest;
import com.yeginamgim.trace.dto.TraceElementResponse;
import com.yeginamgim.trace.dto.TraceElementUpdateRequest;
import com.yeginamgim.trace.dto.TraceImageUploadResponse;
import com.yeginamgim.trace.dto.TraceLikeResponse;
import com.yeginamgim.trace.dto.TraceListResponse;
import com.yeginamgim.trace.dto.TraceResponse;
import com.yeginamgim.trace.dto.TraceUpdateRequest;
import com.yeginamgim.trace.entity.Trace;
import com.yeginamgim.trace.entity.TraceElement;
import com.yeginamgim.trace.entity.TraceLike;
import com.yeginamgim.trace.enums.TraceSortType;
import com.yeginamgim.trace.enums.TraceStatus;
import com.yeginamgim.trace.repository.TraceElementRepository;
import com.yeginamgim.trace.repository.TraceLikeRepository;
import com.yeginamgim.trace.repository.TraceRepository;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TraceService {

    private static final int MAX_TRACE_QUERY_LIMIT = 100;

    private final TraceRepository traceRepository;
    private final TraceElementRepository traceElementRepository;
    private final TraceLikeRepository traceLikeRepository;
    private final BoardRepository boardRepository;
    private final CustomBoardRepository customBoardRepository;
    private final CustomBoardMemberRepository customBoardMemberRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final JWTService jwtService;
    private final PlaceCsvStore placeCsvStore;
    private final NotificationService notificationService;

    // board_id 기준 흔적 목록 조회
    @Transactional(readOnly = true)
    public TraceListResponse getTracesByBoardId(Long boardId, String sort, Integer limit, LocalDateTime before) {
        return getTracesByBoardId(boardId, sort, limit, before, null);
    }

    @Transactional(readOnly = true)
    public TraceListResponse getTracesByBoardId(
            Long boardId,
            String sort,
            Integer limit,
            LocalDateTime before,
            String authorization
    ) {
        BoardEntity board = findBoard(boardId);
        TraceSortType sortType = parseSortType(sort);
        Pageable pageable = toPageable(limit);
        Long viewerUserId = findOptionalUserIdByToken(authorization);

        List<Trace> traces = findBoardTraces(board.getBoardId(), sortType, before, pageable);

        return toTraceListResponse(board, traces, viewerUserId);
    }

    // board_id와 좌표 범위 기준 흔적 목록 조회
    @Transactional(readOnly = true)
    public TraceListResponse getTracesByBoardArea(
            Long boardId,
            Integer minX,
            Integer maxX,
            Integer minY,
            Integer maxY,
            String sort,
            Integer limit,
            LocalDateTime before
    ) {
        return getTracesByBoardArea(boardId, minX, maxX, minY, maxY, sort, limit, before, null);
    }

    @Transactional(readOnly = true)
    public TraceListResponse getTracesByBoardArea(
            Long boardId,
            Integer minX,
            Integer maxX,
            Integer minY,
            Integer maxY,
            String sort,
            Integer limit,
            LocalDateTime before,
            String authorization
    ) {
        validateAreaRange(minX, maxX, minY, maxY);

        BoardEntity board = findBoard(boardId);
        TraceSortType sortType = parseSortType(sort);
        Pageable pageable = toPageable(limit);
        Long viewerUserId = findOptionalUserIdByToken(authorization);

        List<Trace> traces = findBoardAreaTraces(board.getBoardId(), minX, maxX, minY, maxY, sortType, before, pageable);

        return toTraceListResponse(board, traces, viewerUserId);
    }

    private List<Trace> findBoardTraces(Long boardId, TraceSortType sortType, LocalDateTime before, Pageable pageable) {
        return switch (sortType) {
            case OLDEST -> traceRepository.findBoardTracesOldest(boardId, TraceStatus.ACTIVE, before, pageable);
            case POPULAR -> traceRepository.findBoardTracesPopular(boardId, TraceStatus.ACTIVE, before, pageable);
            case LATEST -> traceRepository.findBoardTracesLatest(boardId, TraceStatus.ACTIVE, before, pageable);
        };
    }

    @Transactional(readOnly = true)
    public List<RecentTraceResponse> getRecentTraces(
            String period,
            String district,
            Integer limit,
            String authorization
    ) {
        Pageable pageable = toPageable(limit == null ? 5 : limit);
        LocalDateTime startAt = PeriodRange.startAt(period);
        String normalizedDistrict = normalizeDistrict(district);
        List<String> districtKakaoPlaceIds = findDistrictKakaoPlaceIds(normalizedDistrict);
        Long viewerUserId = findOptionalUserIdByToken(authorization);

        if (StringUtils.hasText(normalizedDistrict) && districtKakaoPlaceIds.isEmpty()) {
            return List.of();
        }

        List<Trace> traces = StringUtils.hasText(normalizedDistrict)
                ? traceRepository.findRecentActiveTracesByKakaoPlaceIdsSince(
                        TraceStatus.ACTIVE,
                        startAt,
                        districtKakaoPlaceIds,
                        pageable
                )
                : traceRepository.findRecentActiveTracesSince(TraceStatus.ACTIVE, startAt, pageable);

        Map<Long, List<TraceElement>> elementMap = findElementsByTraceIds(traces.stream()
                .map(Trace::getTraceId)
                .toList());

        return traces.stream()
                .map(trace -> toRecentTraceResponse(
                        trace,
                        elementMap.getOrDefault(trace.getTraceId(), List.of()),
                        viewerUserId
                ))
                .toList();
    }

    private List<Trace> findBoardAreaTraces(
            Long boardId,
            Integer minX,
            Integer maxX,
            Integer minY,
            Integer maxY,
            TraceSortType sortType,
            LocalDateTime before,
            Pageable pageable
    ) {
        return switch (sortType) {
            case OLDEST -> traceRepository.findBoardAreaTracesOldest(
                    boardId,
                    TraceStatus.ACTIVE,
                    minX,
                    maxX,
                    minY,
                    maxY,
                    before,
                    pageable
            );
            case POPULAR -> traceRepository.findBoardAreaTracesPopular(
                    boardId,
                    TraceStatus.ACTIVE,
                    minX,
                    maxX,
                    minY,
                    maxY,
                    before,
                    pageable
            );
            case LATEST -> traceRepository.findBoardAreaTracesLatest(
                    boardId,
                    TraceStatus.ACTIVE,
                    minX,
                    maxX,
                    minY,
                    maxY,
                    before,
                    pageable
            );
        };
    }

    private TraceListResponse toTraceListResponse(BoardEntity board, List<Trace> traces, Long viewerUserId) {
        List<Long> traceIds = traces.stream()
                .map(Trace::getTraceId)
                .toList();

        Map<Long, List<TraceElement>> elementMap = findElementsByTraceIds(traceIds);

        List<TraceResponse> responses = traces.stream()
                .map(trace -> toTraceResponse(
                        trace,
                        elementMap.getOrDefault(trace.getTraceId(), List.of()),
                        viewerUserId
                ))
                .toList();

        return TraceListResponse.of(board.getBoardId(), responses);
    }

    private Map<Long, List<TraceElement>> findElementsByTraceIds(List<Long> traceIds) {
        if (traceIds.isEmpty()) {
            return Map.of();
        }

        return traceElementRepository
                .findByTrace_TraceIdInOrderByElementIdAsc(traceIds)
                .stream()
                .collect(Collectors.groupingBy(element -> element.getTrace().getTraceId()));
    }

    private RecentTraceResponse toRecentTraceResponse(Trace trace, List<TraceElement> elements, Long viewerUserId) {
        TraceElement firstTextElement = elements.stream()
                .filter(element -> StringUtils.hasText(element.getTextContent()))
                .findFirst()
                .orElse(null);
        TraceElement firstImageElement = elements.stream()
                .filter(element -> StringUtils.hasText(element.getImageUrl()))
                .findFirst()
                .orElse(null);
        String previewText = firstTextElement != null
                ? firstTextElement.getTextContent().trim()
                : firstImageElement != null ? "이미지 흔적" : "남겨진 흔적";
        String imageUrl = firstImageElement == null ? "" : firstImageElement.getImageUrl();
        PlaceInfo placeInfo = placeCsvStore.findByKakaoPlaceId(trace.getBoard().getKakaoPlaceId()).orElse(null);

        return RecentTraceResponse.from(
                trace,
                placeInfo,
                previewText,
                imageUrl,
                traceLikeRepository.countByTrace_TraceId(trace.getTraceId())
        );
    }

    private List<String> findDistrictKakaoPlaceIds(String district) {
        if (!StringUtils.hasText(district)) {
            return List.of();
        }

        return placeCsvStore.findAll().stream()
                .filter(placeInfo -> StringUtils.hasText(placeInfo.getKakaoPlaceId()))
                .filter(placeInfo -> StringUtils.hasText(placeInfo.getAddress()))
                .filter(placeInfo -> placeInfo.getAddress().contains(district))
                .map(PlaceInfo::getKakaoPlaceId)
                .distinct()
                .toList();
    }

    private String normalizeDistrict(String district) {
        if (!StringUtils.hasText(district) || "전체".equals(district.trim())) {
            return "";
        }

        return district.trim();
    }

    // board_id 기준 흔적 생성
    @Transactional
    public TraceResponse createTrace(Long boardId, String authorization, TraceCreateRequest request) {
        validateCreateRequest(request);

        BoardEntity board = findBoard(boardId);
        UserEntity user = findUserByToken(authorization);

        Trace trace = traceRepository.save(Trace.create(board, user, request.getTraceX(), request.getTraceY()));

        List<TraceElement> elements = new ArrayList<>();
        if (request.getElements() != null) {
            for (TraceElementCreateRequest elementRequest : request.getElements()) {
                elements.add(toTraceElement(trace, elementRequest));
            }
        }

        if (!elements.isEmpty()) {
            traceElementRepository.saveAll(elements);
        }

        notificationService.createFollowingTraceNotifications(user, trace);

        return toTraceResponse(trace, elements, user.getUserId());
    }

    // trace_id 기준 흔적 상세 조회
    @Transactional(readOnly = true)
    public TraceResponse getTrace(Long traceId) {
        return getTrace(traceId, null);
    }

    @Transactional(readOnly = true)
    public TraceResponse getTrace(Long traceId, String authorization) {
        Trace trace = findTrace(traceId);
        List<TraceElement> elements = traceElementRepository.findByTrace_TraceIdOrderByElementIdAsc(traceId);
        Long viewerUserId = findOptionalUserIdByToken(authorization);

        return toTraceResponse(trace, elements, viewerUserId);
    }

    // 흔적 이미지 업로드
    public TraceImageUploadResponse uploadTraceImage(MultipartFile file) {
//        String fileName = fileService.boardUpload(file);
        String fileUrl = fileService.boardUpload(file);
//        if (fileName == null) {
        if (fileUrl == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드할 이미지 파일은 필수입니다.");
        }

//        return TraceImageUploadResponse.of("/upload/board/" + fileName);
        return TraceImageUploadResponse.of(fileUrl);
    }

    // trace_id 기준 흔적 수정
    @Transactional
    public TraceResponse updateTrace(Long traceId, String authorization, TraceUpdateRequest request) {
        validateUpdateRequest(request);
        UserEntity user = findUserByToken(authorization);

        Trace trace = traceRepository
                .findByTraceIdAndUser_UserIdAndTraceStatus(traceId, user.getUserId(), TraceStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "수정할 수 있는 흔적을 찾을 수 없습니다."));

        if (request.getTraceX() != null) {
            trace.setTraceX(request.getTraceX());
        }

        if (request.getTraceY() != null) {
            trace.setTraceY(request.getTraceY());
        }

        updateTraceElements(trace, request.getElements());

        List<TraceElement> elements = traceElementRepository.findByTrace_TraceIdOrderByElementIdAsc(traceId);

        return toTraceResponse(trace, elements, user.getUserId());
    }

    // trace_id 기준 흔적 숨김 처리
    @Transactional
    public void hideTrace(Long traceId, String authorization) {
        UserEntity user = findUserByToken(authorization);

        Trace trace = traceRepository
                .findByTraceIdAndUser_UserIdAndTraceStatus(traceId, user.getUserId(), TraceStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "삭제할 수 있는 흔적을 찾을 수 없습니다."));

        trace.hide();
    }

    // custom_board_id 기준 흔적 생성
    @Transactional
    public TraceResponse createTraceForCustomBoard(Long customBoardId, String authorization, TraceCreateRequest request) {
        validateCreateRequest(request);

        UserEntity user = findUserByToken(authorization);
        CustomBoard customBoard = findCustomBoard(customBoardId);
        validateCustomBoardMember(customBoardId, user.getUserId());

        Trace trace = traceRepository.save(Trace.createForCustomBoard(customBoard, user, request.getTraceX(), request.getTraceY()));

        List<TraceElement> elements = new ArrayList<>();
        if (request.getElements() != null) {
            for (TraceElementCreateRequest elementRequest : request.getElements()) {
                elements.add(toTraceElement(trace, elementRequest));
            }
        }

        if (!elements.isEmpty()) {
            traceElementRepository.saveAll(elements);
        }

        notificationService.createFollowingTraceNotifications(user, trace);

        return toCustomBoardTraceResponse(trace, elements, user.getUserId());
    }

    // custom_board_id 기준 흔적 목록 조회
    @Transactional(readOnly = true)
    public List<TraceResponse> getTracesByCustomBoardId(Long customBoardId, String authorization) {
        UserEntity user = findUserByToken(authorization);
        findCustomBoard(customBoardId);
        validateCustomBoardMember(customBoardId, user.getUserId());

        List<Trace> traces = traceRepository.findByCustomBoardId(customBoardId, TraceStatus.ACTIVE);
        List<Long> traceIds = traces.stream().map(Trace::getTraceId).toList();
        Map<Long, List<TraceElement>> elementMap = findElementsByTraceIds(traceIds);

        return traces.stream()
                .map(trace -> toCustomBoardTraceResponse(
                        trace,
                        elementMap.getOrDefault(trace.getTraceId(), List.of()),
                        user.getUserId()
                ))
                .toList();
    }

    // custom_board 흔적 삭제 (작성자 또는 OWNER)
    @Transactional
    public void hideCustomBoardTrace(Long traceId, String authorization) {
        UserEntity user = findUserByToken(authorization);
        Trace trace = findTrace(traceId);

        if (trace.getCustomBoard() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "커스텀 보드 흔적이 아닙니다.");
        }

        Long customBoardId = trace.getCustomBoard().getCustomBoardId();
        boolean isAuthor = trace.getUser().getUserId().equals(user.getUserId());
        boolean isOwner = customBoardMemberRepository
                .existsByCustomBoard_CustomBoardIdAndUser_UserIdAndRole(
                        customBoardId, user.getUserId(), BoardRole.OWNER);

        if (!isAuthor && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "흔적을 삭제할 권한이 없습니다.");
        }

        trace.hide();
    }

    private TraceResponse toCustomBoardTraceResponse(Trace trace, List<TraceElement> elements, Long viewerUserId) {
        List<TraceElementResponse> elementResponses = elements.stream()
                .map(TraceElementResponse::from)
                .toList();

        return TraceResponse.builder()
                .traceId(trace.getTraceId())
                .boardId(trace.getCustomBoard().getCustomBoardId())
                .userId(trace.getUser().getUserId())
                .nickname(trace.getUser().getNickname())
                .traceX(trace.getTraceX())
                .traceY(trace.getTraceY())
                .traceStatus(trace.getTraceStatus().name())
                .createdAt(trace.getCreatedAt())
                .updatedAt(trace.getUpdatedAt())
                .likeCount(traceLikeRepository.countByTrace_TraceId(trace.getTraceId()))
                .liked(isLikedByUser(viewerUserId, trace.getTraceId()))
                .elements(elementResponses)
                .build();
    }

    private CustomBoard findCustomBoard(Long customBoardId) {
        return customBoardRepository.findById(customBoardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "커스텀 보드를 찾을 수 없습니다."));
    }

    private void validateCustomBoardMember(Long customBoardId, Long userId) {
        if (!customBoardMemberRepository.existsByCustomBoard_CustomBoardIdAndUser_UserId(customBoardId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "보드 멤버만 흔적을 남길 수 있습니다.");
        }
    }

    // trace_id 기준 추천 등록
    @Transactional
    public TraceLikeResponse addLike(Long traceId, String authorization) {
        Trace trace = findTrace(traceId);
        UserEntity user = findUserByToken(authorization);
        Long userId = user.getUserId();

        if (!traceLikeRepository.existsByUser_UserIdAndTrace_TraceId(userId, traceId)) {
            traceLikeRepository.save(TraceLike.create(user, trace));
        }

        return toLikeResponse(traceId, true);
    }

    // trace_id 기준 추천 취소
    @Transactional
    public TraceLikeResponse removeLike(Long traceId, String authorization) {
        findTrace(traceId);
        Long userId = findUserByToken(authorization).getUserId();

        if (traceLikeRepository.existsByUser_UserIdAndTrace_TraceId(userId, traceId)) {
            traceLikeRepository.deleteByUser_UserIdAndTrace_TraceId(userId, traceId);
        }

        return toLikeResponse(traceId, false);
    }

    private void validateCreateRequest(TraceCreateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "흔적 생성 요청은 필수입니다.");
        }

        if (request.getTraceX() == null || request.getTraceY() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "흔적 좌표는 필수입니다.");
        }
    }

    private void validateUpdateRequest(TraceUpdateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "흔적 수정 요청은 필수입니다.");
        }
    }

    private void validateAreaRange(Integer minX, Integer maxX, Integer minY, Integer maxY) {
        if (minX == null || maxX == null || minY == null || maxY == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "조회할 좌표 범위는 필수입니다.");
        }

        if (minX > maxX || minY > maxY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "최소 좌표는 최대 좌표보다 클 수 없습니다.");
        }
    }

    private TraceSortType parseSortType(String sort) {
        if (sort == null || sort.isBlank()) {
            return TraceSortType.LATEST;
        }

        try {
            return TraceSortType.valueOf(sort.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sort는 latest, oldest, popular 중 하나여야 합니다.");
        }
    }

    private Pageable toPageable(Integer limit) {
        if (limit == null) {
            return Pageable.unpaged();
        }

        if (limit <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit은 1 이상이어야 합니다.");
        }

        if (limit > MAX_TRACE_QUERY_LIMIT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit은 최대 100까지 가능합니다.");
        }

        return PageRequest.of(0, limit);
    }

    private BoardEntity findBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "보드를 찾을 수 없습니다."));
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

    private Long findOptionalUserIdByToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }

        return findUserByToken(authorization).getUserId();
    }

    private Trace findTrace(Long traceId) {
        return traceRepository.findById(traceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "흔적을 찾을 수 없습니다."));
    }

    private TraceElement toTraceElement(Trace trace, TraceElementCreateRequest request) {
        if (request == null || request.getContentType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "흔적 요소의 contentType은 필수입니다.");
        }

        return TraceElement.create(
                trace,
                request.getContentType(),
                request.getTextContent(),
                request.getImageUrl(),
                request.getElementX(),
                request.getElementY(),
                request.getStyleJson()
        );
    }

    private void updateTraceElements(Trace trace, List<TraceElementUpdateRequest> elementRequests) {
        if (elementRequests == null || elementRequests.isEmpty()) {
            return;
        }

        List<TraceElement> existingElements = traceElementRepository.findByTrace_TraceIdOrderByElementIdAsc(trace.getTraceId());
        Map<Long, TraceElement> existingElementMap = existingElements.stream()
                .collect(Collectors.toMap(TraceElement::getElementId, Function.identity()));

        List<TraceElement> newElements = new ArrayList<>();

        for (TraceElementUpdateRequest elementRequest : elementRequests) {
            if (elementRequest == null) {
                continue;
            }

            if (elementRequest.getElementId() == null) {
                newElements.add(toTraceElement(trace, elementRequest));
                continue;
            }

            TraceElement element = existingElementMap.get(elementRequest.getElementId());
            if (element == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "수정할 흔적 요소를 찾을 수 없습니다.");
            }

            updateTraceElement(element, elementRequest);
        }

        if (!newElements.isEmpty()) {
            traceElementRepository.saveAll(newElements);
        }
    }

    private TraceElement toTraceElement(Trace trace, TraceElementUpdateRequest request) {
        if (request.getContentType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "새 흔적 요소의 contentType은 필수입니다.");
        }

        return TraceElement.create(
                trace,
                request.getContentType(),
                request.getTextContent(),
                request.getImageUrl(),
                request.getElementX(),
                request.getElementY(),
                request.getStyleJson()
        );
    }

    private void updateTraceElement(TraceElement element, TraceElementUpdateRequest request) {
        if (request.getContentType() != null) {
            element.setContentType(request.getContentType());
        }

        if (request.getTextContent() != null) {
            element.setTextContent(request.getTextContent());
        }

        if (request.getImageUrl() != null) {
            element.setImageUrl(request.getImageUrl());
        }

        if (request.getElementX() != null) {
            element.setElementX(request.getElementX());
        }

        if (request.getElementY() != null) {
            element.setElementY(request.getElementY());
        }

        if (request.getStyleJson() != null) {
            element.setStyleJson(request.getStyleJson());
        }
    }

    private TraceResponse toTraceResponse(Trace trace, List<TraceElement> elements) {
        return toTraceResponse(trace, elements, null);
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

    private TraceLikeResponse toLikeResponse(Long traceId, boolean liked) {
        return TraceLikeResponse.of(traceId, liked, traceLikeRepository.countByTrace_TraceId(traceId));
    }
}
