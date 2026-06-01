package com.yeginamgim.trace.service;

import com.yeginamgim.board.entity.BoardEntity;
import com.yeginamgim.board.repository.BoardRepository;
import com.yeginamgim.global.file.FileService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TraceService {

    private final TraceRepository traceRepository;
    private final TraceElementRepository traceElementRepository;
    private final TraceLikeRepository traceLikeRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    // board_id 기준 흔적 목록 조회
    @Transactional(readOnly = true)
    public TraceListResponse getTracesByBoardId(Long boardId) {
        BoardEntity board = findBoard(boardId);

        List<Trace> traces = traceRepository
                .findByBoard_BoardIdAndTraceStatusOrderByCreatedAtDescTraceIdDesc(
                        board.getBoardId(),
                        TraceStatus.ACTIVE
                );

        List<Long> traceIds = traces.stream()
                .map(Trace::getTraceId)
                .toList();

        Map<Long, List<TraceElement>> elementMap = traceIds.isEmpty()
                ? Map.of()
                : traceElementRepository
                        .findByTrace_TraceIdInOrderByElementIdAsc(traceIds)
                        .stream()
                        .collect(Collectors.groupingBy(element -> element.getTrace().getTraceId()));

        List<TraceResponse> responses = traces.stream()
                .map(trace -> toTraceResponse(
                        trace,
                        elementMap.getOrDefault(trace.getTraceId(), List.of())
                ))
                .toList();

        return TraceListResponse.builder()
                .boardId(board.getBoardId())
                .traces(responses)
                .build();
    }

    // board_id 기준 흔적 생성
    @Transactional
    public TraceResponse createTrace(Long boardId, TraceCreateRequest request) {
        validateCreateRequest(request);

        BoardEntity board = findBoard(boardId);
        UserEntity user = findUser(request.getUserId());

        Trace trace = traceRepository.save(Trace.builder()
                .board(board)
                .user(user)
                .traceX(request.getTraceX())
                .traceY(request.getTraceY())
                .traceStatus(TraceStatus.ACTIVE)
                .build());

        List<TraceElement> elements = new ArrayList<>();
        if (request.getElements() != null) {
            for (TraceElementCreateRequest elementRequest : request.getElements()) {
                elements.add(toTraceElement(trace, elementRequest));
            }
        }

        if (!elements.isEmpty()) {
            traceElementRepository.saveAll(elements);
        }

        return toTraceResponse(trace, elements);
    }

    // trace_id 기준 흔적 상세 조회
    @Transactional(readOnly = true)
    public TraceResponse getTrace(Long traceId) {
        Trace trace = findTrace(traceId);
        List<TraceElement> elements = traceElementRepository.findByTrace_TraceIdOrderByElementIdAsc(traceId);

        return toTraceResponse(trace, elements);
    }

    // 흔적 이미지 업로드
    public TraceImageUploadResponse uploadTraceImage(MultipartFile file) {
        String fileName = fileService.boardUpload(file);
        if (fileName == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드할 이미지 파일은 필수입니다.");
        }

        return TraceImageUploadResponse.builder()
                .imageUrl("/upload/board/" + fileName)
                .build();
    }

    // trace_id 기준 흔적 수정
    @Transactional
    public TraceResponse updateTrace(Long traceId, TraceUpdateRequest request) {
        validateUpdateRequest(request);

        Trace trace = traceRepository
                .findByTraceIdAndUser_UserIdAndTraceStatus(traceId, request.getUserId(), TraceStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "수정할 수 있는 흔적을 찾을 수 없습니다."));

        if (request.getTraceX() != null) {
            trace.setTraceX(request.getTraceX());
        }

        if (request.getTraceY() != null) {
            trace.setTraceY(request.getTraceY());
        }

        updateTraceElements(trace, request.getElements());

        List<TraceElement> elements = traceElementRepository.findByTrace_TraceIdOrderByElementIdAsc(traceId);

        return toTraceResponse(trace, elements);
    }

    // trace_id 기준 흔적 숨김 처리
    @Transactional
    public void hideTrace(Long traceId) {
        Trace trace = findTrace(traceId);
        trace.hide();
    }

    // trace_id 기준 추천 등록
    @Transactional
    public TraceLikeResponse addLike(Long traceId, Long userId) {
        Trace trace = findTrace(traceId);
        validateUserId(userId);
        UserEntity user = findUser(userId);

        if (!traceLikeRepository.existsByUser_UserIdAndTrace_TraceId(userId, traceId)) {
            traceLikeRepository.save(TraceLike.builder()
                    .trace(trace)
                    .user(user)
                    .build());
        }

        return toLikeResponse(traceId, true);
    }

    // trace_id 기준 추천 취소
    @Transactional
    public TraceLikeResponse removeLike(Long traceId, Long userId) {
        findTrace(traceId);
        validateUserId(userId);
        findUser(userId);

        if (traceLikeRepository.existsByUser_UserIdAndTrace_TraceId(userId, traceId)) {
            traceLikeRepository.deleteByUser_UserIdAndTrace_TraceId(userId, traceId);
        }

        return toLikeResponse(traceId, false);
    }

    private void validateCreateRequest(TraceCreateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "흔적 생성 요청은 필수입니다.");
        }

        if (request.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId는 필수입니다.");
        }

        if (request.getTraceX() == null || request.getTraceY() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "흔적 좌표는 필수입니다.");
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId는 필수입니다.");
        }
    }

    private void validateUpdateRequest(TraceUpdateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "흔적 수정 요청은 필수입니다.");
        }

        validateUserId(request.getUserId());
    }

    private BoardEntity findBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "보드를 찾을 수 없습니다."));
    }

    private UserEntity findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private Trace findTrace(Long traceId) {
        return traceRepository.findById(traceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "흔적을 찾을 수 없습니다."));
    }

    private TraceElement toTraceElement(Trace trace, TraceElementCreateRequest request) {
        if (request == null || request.getContentType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "흔적 요소의 contentType은 필수입니다.");
        }

        return TraceElement.builder()
                .trace(trace)
                .contentType(request.getContentType())
                .textContent(request.getTextContent())
                .imageUrl(request.getImageUrl())
                .elementX(request.getElementX())
                .elementY(request.getElementY())
                .styleJson(request.getStyleJson())
                .build();
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

        return TraceElement.builder()
                .trace(trace)
                .contentType(request.getContentType())
                .textContent(request.getTextContent())
                .imageUrl(request.getImageUrl())
                .elementX(request.getElementX())
                .elementY(request.getElementY())
                .styleJson(request.getStyleJson())
                .build();
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
        return TraceResponse.builder()
                .traceId(trace.getTraceId())
                .boardId(trace.getBoard().getBoardId())
                .userId(trace.getUser().getUserId())
                .nickname(trace.getUser().getNickname())
                .traceX(trace.getTraceX())
                .traceY(trace.getTraceY())
                .traceStatus(trace.getTraceStatus().name())
                .createdAt(trace.getCreatedAt())
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
                .build();
    }

    private TraceLikeResponse toLikeResponse(Long traceId, boolean liked) {
        return TraceLikeResponse.builder()
                .traceId(traceId)
                .liked(liked)
                .likeCount(traceLikeRepository.countByTrace_TraceId(traceId))
                .build();
    }
}
