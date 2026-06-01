package com.yeginamgim.place.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceConfirmRequest {
    private String kakaoPlaceId;
    private String placeName;
    private Double latitude;
    private Double longitude;
    private String phone;
    private String address;
    private String kakaoMapUrl;
    private String groupName;
}
