package com.yeginamgim.board.controller;

import com.yeginamgim.board.dto.BoardCreateRequest;
import com.yeginamgim.board.dto.BoardDetailResponse;
import com.yeginamgim.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// [3] 공간 보드 시스템
@RestController
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // kakao_place_id 기준 보드 조회 또는 생성
    @GetMapping("/api/places/{kakaoPlaceId}/board")
    public BoardDetailResponse getBoardByKakaoPlaceId(@PathVariable String kakaoPlaceId) {
        return boardService.getOrCreateBoardByKakaoPlaceId(kakaoPlaceId);
    }

    // (1) 공간별 디지털 보드 조회
    // board_id를 기준으로 보드 정보와 CSV 장소 정보를 함께 반환
    @GetMapping("/api/boards/{boardId}")
    public BoardDetailResponse getBoardDetail(@PathVariable Long boardId) {
        return boardService.getBoardDetail(boardId);
    }

    // kakao_place_id 기준 보드 생성 또는 기존 보드 반환
    @PostMapping("/api/boards")
    public BoardDetailResponse createBoard(@RequestBody BoardCreateRequest request) {
        return boardService.createBoard(request);
    }

    // (2) 보드 안의 흔적 위치 기반 조회
    // (3) 오래된 흔적 유지 및 탐색
    // 위 기능은 trace 담당 영역에서 구현한다.
}
