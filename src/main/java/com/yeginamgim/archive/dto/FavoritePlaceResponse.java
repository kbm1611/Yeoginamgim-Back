package com.yeginamgim.archive.dto;

import com.yeginamgim.archive.entity.FavoritePlace;
import com.yeginamgim.board.dto.PlaceInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

// 즐겨찾기한 장소 응답 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoritePlaceResponse {
    private Long favoritePlaceId;
    private Long userId;
    private String kakaoPlaceId;
    private String placeName;
    private Double latitude;
    private Double longitude;
    private String phone;
    private String address;
    private String kakaoMapUrl;
    private String groupName;
    private Long boardId;
    private Instant createdAt;
    private Instant updatedAt;

    public static FavoritePlaceResponse from(FavoritePlace favoritePlace, PlaceInfo placeInfo, Long boardId) {
        return FavoritePlaceResponse.builder()
                .favoritePlaceId(favoritePlace.getFavoritePlaceId())
                .userId(favoritePlace.getUser().getUserId())
                .kakaoPlaceId(favoritePlace.getKakaoPlaceId())
                .placeName(placeInfo.getPlaceName())
                .latitude(placeInfo.getLatitude())
                .longitude(placeInfo.getLongitude())
                .phone(placeInfo.getPhone())
                .address(placeInfo.getAddress())
                .kakaoMapUrl(placeInfo.getKakaoMapUrl())
                .groupName(placeInfo.getGroupName())
                .boardId(boardId)
                .createdAt(favoritePlace.getCreatedAt())
                .updatedAt(favoritePlace.getUpdatedAt())
                .build();
    }
}
