package com.yeginamgim.board.dto;

import java.time.LocalDateTime;

public class BoardDto {

    // CSV에서 읽어온 장소 상세 정보를 담는 응답 DTO
    public record PlaceInfo(
            String kakaoPlaceId,
            String placeName,
            Double latitude,
            Double longitude,
            String phone,
            String address,
            String kakaoMapUrl,
            String groupName
    ) {
    }

    // 보드 기본 정보와 장소 상세 정보를 함께 반환하는 응답 DTO
    public record BoardDetailResponse(
            Long boardId,
            String kakaoPlaceId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            PlaceInfo place
    ) {
    }
}
