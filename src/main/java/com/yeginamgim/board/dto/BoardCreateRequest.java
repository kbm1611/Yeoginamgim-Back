package com.yeginamgim.board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 보드 생성 요청 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardCreateRequest {
    private String kakaoPlaceId;
    private String placeName;
    private Double latitude;
    private Double longitude;
    private String phone;
    private String address;
    private String kakaoMapUrl;
    private String groupName;
}
