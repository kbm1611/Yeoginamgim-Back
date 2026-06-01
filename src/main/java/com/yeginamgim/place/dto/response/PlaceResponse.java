package com.yeginamgim.place.dto.response;

import com.yeginamgim.board.dto.PlaceInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceResponse {
    private String kakaoPlaceId;
    private String placeName;
    private Double latitude;
    private Double longitude;
    private String phone;
    private String address;
    private String kakaoMapUrl;
    private String groupName;
    private Long traceCount;
    private Long boardId;

    public static PlaceResponse from(PlaceInfo placeInfo, Long traceCount, Long boardId) {
        return PlaceResponse.builder()
                .kakaoPlaceId(placeInfo.getKakaoPlaceId())
                .placeName(placeInfo.getPlaceName())
                .latitude(placeInfo.getLatitude())
                .longitude(placeInfo.getLongitude())
                .phone(placeInfo.getPhone())
                .address(placeInfo.getAddress())
                .kakaoMapUrl(placeInfo.getKakaoMapUrl())
                .groupName(placeInfo.getGroupName())
                .traceCount(traceCount)
                .boardId(boardId)
                .build();
    }
}
