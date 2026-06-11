package com.yeginamgim.board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// CSV 장소 상세 정보 응답 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PlaceInfo {
    private String kakaoPlaceId;
    private String placeName;
    private Double latitude;
    private Double longitude;
    private String phone;
    private String address;
    private String kakaoMapUrl;
    private String groupName;

    public static PlaceInfo from(BoardCreateRequest request) {
        return PlaceInfo.builder()
                .kakaoPlaceId(request.getKakaoPlaceId())
                .placeName(request.getPlaceName())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .phone(request.getPhone())
                .address(request.getAddress())
                .kakaoMapUrl(request.getKakaoMapUrl())
                .groupName(request.getGroupName())
                .build();
    }
}
