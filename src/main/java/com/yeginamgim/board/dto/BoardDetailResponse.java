package com.yeginamgim.board.dto;

import com.yeginamgim.board.entity.BoardEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

// 보드 기본 정보와 장소 상세 정보 응답 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardDetailResponse {
    private Long boardId;
    private String kakaoPlaceId;
    private Instant createdAt;
    private Long traceCount;
    private PlaceInfo place;

    public static BoardDetailResponse from(BoardEntity board, PlaceInfo place, Long traceCount) {
        return BoardDetailResponse.builder()
                .boardId(board.getBoardId())
                .kakaoPlaceId(board.getKakaoPlaceId())
                .createdAt(board.getCreatedAt())
                .traceCount(traceCount)
                .place(place)
                .build();
    }
}
