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
public class PopularPlaceResponse {
    private Integer rank;
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

    // 내부 장소 정보와 인기 순위 데이터를 API 응답 DTO로 변환한다.
    public static PopularPlaceResponse from(int rank, PlaceInfo placeInfo, Long traceCount, Long boardId) {
        return PopularPlaceResponse.builder()
                .rank(rank)
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
