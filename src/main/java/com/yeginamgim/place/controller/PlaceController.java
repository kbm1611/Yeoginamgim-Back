package com.yeginamgim.place.controller;

import com.yeginamgim.place.dto.request.PlaceSearchRequest;
import com.yeginamgim.place.dto.response.PlaceResponse;
import com.yeginamgim.place.dto.response.PopularPlaceResponse;
import com.yeginamgim.place.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
public class PlaceController {

    private final PlaceService placeService;

    // 근처 장소 조회
    @GetMapping("/nearby")
    public List<PlaceResponse> getNearbyPlaces(@ModelAttribute PlaceSearchRequest request) {
        return placeService.searchNearbyPlaces(request);
    }

    // 인기 장소 조회
    @GetMapping("/popular")
    public List<PopularPlaceResponse> getPopularPlaces(
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        return placeService.getPopularPlaces(limit);
    }

    // kakaoId로 장소 조회
    @GetMapping("/{kakaoPlaceId}")
    public PlaceResponse getPlace(@PathVariable String kakaoPlaceId) {
        return placeService.getPlaceByKakaoPlaceId(kakaoPlaceId);
    }
}
