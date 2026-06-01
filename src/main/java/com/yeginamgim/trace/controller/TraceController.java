package com.yeginamgim.trace.controller;

import com.yeginamgim.trace.dto.TraceCreateRequest;
import com.yeginamgim.trace.dto.TraceImageUploadResponse;
import com.yeginamgim.trace.dto.TraceLikeResponse;
import com.yeginamgim.trace.dto.TraceListResponse;
import com.yeginamgim.trace.dto.TraceResponse;
import com.yeginamgim.trace.dto.TraceUpdateRequest;
import com.yeginamgim.trace.service.TraceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

// [3] 공간 보드 위 흔적 기능
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TraceController {

    private final TraceService traceService;

    // board_id 기준 흔적 목록 조회
    @GetMapping("/boards/{boardId}/traces")
    public TraceListResponse getTracesByBoardId(
            @PathVariable Long boardId,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime before
    ) {
        return traceService.getTracesByBoardId(boardId, sort, limit, before);
    }

    // board_id와 좌표 범위 기준 흔적 목록 조회
    // 예: /api/boards/3/traces/area?minX=0&maxX=100&minY=0&maxY=100&sort=latest&limit=20
    @GetMapping("/boards/{boardId}/traces/area")
    public TraceListResponse getTracesByBoardArea(
            @PathVariable Long boardId,
            @RequestParam Integer minX,
            @RequestParam Integer maxX,
            @RequestParam Integer minY,
            @RequestParam Integer maxY,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime before
    ) {
        return traceService.getTracesByBoardArea(boardId, minX, maxX, minY, maxY, sort, limit, before);
    }

    // board_id 기준 흔적 생성
    @PostMapping("/boards/{boardId}/traces")
    public TraceResponse createTrace(
            @PathVariable Long boardId,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody TraceCreateRequest request
    ) {
        return traceService.createTrace(boardId, authorization, request);
    }

    // trace_id 기준 흔적 상세 조회
    @GetMapping("/traces/{traceId}")
    public TraceResponse getTrace(@PathVariable Long traceId) {
        return traceService.getTrace(traceId);
    }

    // 흔적 이미지 업로드
    @PostMapping("/traces/images")
    public TraceImageUploadResponse uploadTraceImage(@RequestParam("file") MultipartFile file) {
        return traceService.uploadTraceImage(file);
    }

    // trace_id 기준 흔적 수정
    @PatchMapping("/traces/{traceId}")
    public TraceResponse updateTrace(
            @PathVariable Long traceId,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody TraceUpdateRequest request
    ) {
        return traceService.updateTrace(traceId, authorization, request);
    }

    // trace_id 기준 흔적 숨김 처리
    @DeleteMapping("/traces/{traceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hideTrace(
            @PathVariable Long traceId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        traceService.hideTrace(traceId, authorization);
    }

    // trace_id 기준 추천 등록
    @PostMapping("/traces/{traceId}/likes")
    public TraceLikeResponse addLike(
            @PathVariable Long traceId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return traceService.addLike(traceId, authorization);
    }

    // trace_id 기준 추천 취소
    @DeleteMapping("/traces/{traceId}/likes")
    public TraceLikeResponse removeLike(
            @PathVariable Long traceId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return traceService.removeLike(traceId, authorization);
    }
}
