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

    @GetMapping("/nearby")
    public List<PlaceResponse> getNearbyPlaces(@ModelAttribute PlaceSearchRequest request) {
        return placeService.searchNearbyPlaces(request);
    }

    @GetMapping("/popular")
    public List<PopularPlaceResponse> getPopularPlaces(
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        return placeService.getPopularPlaces(limit);
    }

    @GetMapping("/{kakaoPlaceId}")
    public PlaceResponse getPlace(@PathVariable String kakaoPlaceId) {
        return placeService.getPlaceByKakaoPlaceId(kakaoPlaceId);
    }
}
