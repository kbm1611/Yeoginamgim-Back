package com.yeginamgim.trace.controller;

import com.yeginamgim.trace.dto.TraceCreateRequest;
import com.yeginamgim.trace.dto.TraceLikeRequest;
import com.yeginamgim.trace.dto.TraceLikeResponse;
import com.yeginamgim.trace.dto.TraceListResponse;
import com.yeginamgim.trace.dto.TraceResponse;
import com.yeginamgim.trace.service.TraceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// [3] 공간 보드 위 흔적 기능
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TraceController {

    private final TraceService traceService;

    // board_id 기준 흔적 목록 조회
    @GetMapping("/boards/{boardId}/traces")
    public TraceListResponse getTracesByBoardId(@PathVariable Long boardId) {
        return traceService.getTracesByBoardId(boardId);
    }

    // board_id 기준 흔적 생성
    @PostMapping("/boards/{boardId}/traces")
    public TraceResponse createTrace(
            @PathVariable Long boardId,
            @RequestBody TraceCreateRequest request
    ) {
        return traceService.createTrace(boardId, request);
    }

    // trace_id 기준 흔적 상세 조회
    @GetMapping("/traces/{traceId}")
    public TraceResponse getTrace(@PathVariable Long traceId) {
        return traceService.getTrace(traceId);
    }

    // trace_id 기준 흔적 숨김 처리
    @DeleteMapping("/traces/{traceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hideTrace(@PathVariable Long traceId) {
        traceService.hideTrace(traceId);
    }

    // trace_id 기준 추천 등록
    @PostMapping("/traces/{traceId}/likes")
    public TraceLikeResponse addLike(
            @PathVariable Long traceId,
            @RequestBody TraceLikeRequest request
    ) {
        return traceService.addLike(traceId, request.getUserId());
    }

    // trace_id 기준 추천 취소
    @DeleteMapping("/traces/{traceId}/likes")
    public TraceLikeResponse removeLike(
            @PathVariable Long traceId,
            @RequestParam Long userId
    ) {
        return traceService.removeLike(traceId, userId);
    }
}
