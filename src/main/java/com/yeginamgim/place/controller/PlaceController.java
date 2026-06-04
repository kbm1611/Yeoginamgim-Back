package com.yeginamgim.place.controller;

import com.yeginamgim.place.dto.request.PlaceSearchRequest;
import com.yeginamgim.place.dto.response.PlaceResponse;
import com.yeginamgim.place.dto.response.PopularPlaceResponse;
import com.yeginamgim.place.service.PlaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
public class PlaceController {

    private final PlaceService placeService;

    // 현재 위치와 카테고리를 기준으로 주변 장소 목록을 조회한다.
    @GetMapping("/nearby")
    public List<PlaceResponse> getNearbyPlaces(@Valid @ModelAttribute PlaceSearchRequest request) {
        return placeService.searchNearbyPlaces(request);
    }

    // 검색한 장소 조회
    @GetMapping("/search")
    public List<PlaceResponse> searchPlaces(@ModelAttribute PlaceSearchRequest request) {
        return placeService.searchPlacesByKeyword(request);
    }

    // active 흔적 수가 많은 장소를 인기 장소 순위로 조회한다.
    @GetMapping("/popular")
    public List<PopularPlaceResponse> getPopularPlaces(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String district,
            @RequestParam(defaultValue = "today") String period,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Integer radius
    ) {
        return placeService.getPopularPlaces(limit, district, latitude, longitude, radius, period);
    }
}
