package com.yeginamgim.archive.controller;

import com.yeginamgim.archive.dto.ArchiveBoardListResponse;
import com.yeginamgim.archive.dto.ArchiveCalendarResponse;
import com.yeginamgim.archive.dto.ArchiveTraceListResponse;
import com.yeginamgim.archive.dto.FavoritePlaceListResponse;
import com.yeginamgim.archive.dto.FavoritePlaceRequest;
import com.yeginamgim.archive.dto.FavoritePlaceResponse;
import com.yeginamgim.archive.service.ArchiveService;
import com.yeginamgim.trace.dto.TraceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// [4] 보관함 기능
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ArchiveController {

    private final ArchiveService archiveService;

    // 내가 남긴 흔적 전체 조회
    @GetMapping("/me/traces")
    public ArchiveTraceListResponse getMyTraces(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return archiveService.getMyTraces(authorization);
    }

    // 내가 남긴 흔적 개별 조회
    @GetMapping("/me/traces/{traceId}")
    public TraceResponse getMyTrace(
            @PathVariable Long traceId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return archiveService.getMyTrace(authorization, traceId);
    }

    // 날짜별 기록 조회
    @GetMapping("/me/archive/calendar")
    public ArchiveCalendarResponse getCalendar(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return archiveService.getCalendar(authorization, year, month);
    }

    // 공간별 추억 아카이브 조회
    @GetMapping("/me/archive/boards")
    public ArchiveBoardListResponse getBoardArchives(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return archiveService.getBoardArchives(authorization);
    }

    // 내가 작성한 흔적 중 좋아요를 받은 흔적 조회
    @GetMapping("/me/received-likes")
    public ArchiveTraceListResponse getReceivedLikeTraces(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return archiveService.getReceivedLikeTraces(authorization);
    }

    // 내가 즐겨찾기한 장소 목록 조회
    @GetMapping("/me/archive/favorite-places")
    public FavoritePlaceListResponse getFavoritePlaces(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return archiveService.getFavoritePlaces(authorization);
    }

    // 장소 즐겨찾기 등록
    @PostMapping("/me/archive/favorite-places/{kakaoPlaceId}")
    @ResponseStatus(HttpStatus.CREATED)
    public FavoritePlaceResponse addFavoritePlace(
            @PathVariable String kakaoPlaceId,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) FavoritePlaceRequest request
    ) {
        return archiveService.addFavoritePlace(authorization, kakaoPlaceId, request);
    }

    // 장소 즐겨찾기 취소
    @DeleteMapping("/me/archive/favorite-places/{kakaoPlaceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavoritePlace(
            @PathVariable String kakaoPlaceId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        archiveService.removeFavoritePlace(authorization, kakaoPlaceId);
    }
}
