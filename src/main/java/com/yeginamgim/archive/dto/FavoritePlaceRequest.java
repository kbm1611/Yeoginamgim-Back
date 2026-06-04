package com.yeginamgim.archive.dto;

import lombok.Data;

// 즐겨찾기 등록 시 CSV 캐시에 저장할 수 있는 장소 스냅샷 요청 DTO
@Data
public class FavoritePlaceRequest {
    private String placeName;
    private Double latitude;
    private Double longitude;
    private String phone;
    private String address;
    private String kakaoMapUrl;
    private String groupName;
}
