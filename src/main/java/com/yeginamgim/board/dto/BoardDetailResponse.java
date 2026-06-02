package com.yeginamgim.board.dto;

import com.yeginamgim.board.entity.BoardEntity;
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

    public static BoardDetailResponse from(BoardEntity board, PlaceInfo place) {
        return BoardDetailResponse.builder()
                .boardId(board.getBoardId())
                .kakaoPlaceId(board.getKakaoPlaceId())
                .createdAt(board.getCreatedAt())
                .place(place)
                .build();
    }
}
