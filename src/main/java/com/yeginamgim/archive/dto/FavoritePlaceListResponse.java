package com.yeginamgim.archive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// 즐겨찾기한 장소 목록 응답 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoritePlaceListResponse {
    private Long userId;

    @Builder.Default
    private List<FavoritePlaceResponse> places = new ArrayList<>();

    public static FavoritePlaceListResponse of(Long userId, List<FavoritePlaceResponse> places) {
        return FavoritePlaceListResponse.builder()
                .userId(userId)
                .places(places)
                .build();
    }
}
