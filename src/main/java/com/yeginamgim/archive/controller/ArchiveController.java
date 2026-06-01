package com.yeginamgim.archive.controller;

import com.yeginamgim.archive.dto.ArchiveBoardListResponse;
import com.yeginamgim.archive.dto.ArchiveCalendarResponse;
import com.yeginamgim.archive.dto.ArchiveTraceListResponse;
import com.yeginamgim.archive.service.ArchiveService;
import com.yeginamgim.trace.dto.TraceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// [4] 보관함 기능
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ArchiveController {

    private final ArchiveService archiveService;

    // 내가 남긴 흔적 전체 조회
    @GetMapping("/users/{userId}/traces")
    public ArchiveTraceListResponse getMyTraces(@PathVariable Long userId) {
        return archiveService.getMyTraces(userId);
    }

    // 내가 남긴 흔적 개별 조회
    @GetMapping("/users/{userId}/traces/{traceId}")
    public TraceResponse getMyTrace(
            @PathVariable Long userId,
            @PathVariable Long traceId
    ) {
        return archiveService.getMyTrace(userId, traceId);
    }

    // 날짜별 기록 조회
    @GetMapping("/users/{userId}/archive/calendar")
    public ArchiveCalendarResponse getCalendar(
            @PathVariable Long userId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return archiveService.getCalendar(userId, year, month);
    }

    // 공간별 추억 아카이브 조회
    @GetMapping("/users/{userId}/archive/boards")
    public ArchiveBoardListResponse getBoardArchives(@PathVariable Long userId) {
        return archiveService.getBoardArchives(userId);
    }

    // 내가 작성한 흔적 중 좋아요를 받은 흔적 조회
    @GetMapping("/users/{userId}/received-likes")
    public ArchiveTraceListResponse getReceivedLikeTraces(@PathVariable Long userId) {
        return archiveService.getReceivedLikeTraces(userId);
    }
}
