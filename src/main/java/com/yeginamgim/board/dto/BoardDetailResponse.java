package com.yeginamgim.board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 보드 기본 정보와 장소 상세 정보 응답 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardDetailResponse {
    private Long boardId;
    private String kakaoPlaceId;
    private LocalDateTime createdAt;
    private PlaceInfo place;
}
