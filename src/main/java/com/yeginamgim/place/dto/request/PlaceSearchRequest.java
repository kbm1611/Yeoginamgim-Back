package com.yeginamgim.place.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceSearchRequest {
    private Double latitude;
    private Double longitude;
    private Integer radius;
    private String category;
    private String query;
    private Integer page;
    private Integer limit;
}
