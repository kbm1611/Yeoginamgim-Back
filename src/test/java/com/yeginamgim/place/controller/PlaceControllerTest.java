package com.yeginamgim.place.controller;

import com.yeginamgim.place.dto.request.PlaceSearchRequest;
import com.yeginamgim.place.service.PlaceService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class PlaceControllerTest {

    private final PlaceService placeService = mock(PlaceService.class);
    private final MockMvc mockMvc = standaloneSetup(new PlaceController(placeService)).build();

    @Test
    void doesNotExposeSinglePlaceLookupByKakaoPlaceId() throws Exception {
        mockMvc.perform(get("/api/places/cache-board"))
                .andExpect(status().isNotFound());
    }

    @Test
    void popularPlacesAcceptDistrictQueryParameter() throws Exception {
        when(placeService.getPopularPlaces(10, "Gangnam", null, null, null, "today")).thenReturn(List.of());

        mockMvc.perform(get("/api/places/popular")
                        .param("district", "Gangnam")
                        .param("limit", "10"))
                .andExpect(status().isOk());

        verify(placeService).getPopularPlaces(10, "Gangnam", null, null, null, "today");
    }

    @Test
    void popularPlacesAcceptLocationQueryParameters() throws Exception {
        when(placeService.getPopularPlaces(10, null, 37.5447, 127.0559, 20000, "today")).thenReturn(List.of());

        mockMvc.perform(get("/api/places/popular")
                        .param("latitude", "37.5447")
                        .param("longitude", "127.0559")
                        .param("radius", "20000")
                        .param("limit", "10"))
                .andExpect(status().isOk());

        verify(placeService).getPopularPlaces(10, null, 37.5447, 127.0559, 20000, "today");
    }

    @Test
    void popularPlacesAcceptPeriodQueryParameter() throws Exception {
        when(placeService.getPopularPlaces(5, "Seongdong", null, null, null, "today")).thenReturn(List.of());

        mockMvc.perform(get("/api/places/popular")
                        .param("district", "Seongdong")
                        .param("period", "today")
                        .param("limit", "5"))
                .andExpect(status().isOk());

        verify(placeService).getPopularPlaces(5, "Seongdong", null, null, null, "today");
    }

    @Test
    void placeSearchAcceptsKeywordLocationRadiusAndLimitParameters() throws Exception {
        when(placeService.searchPlacesByKeyword(org.mockito.ArgumentMatchers.any(PlaceSearchRequest.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/places/search")
                        .param("query", "coffee")
                        .param("latitude", "37.5447")
                        .param("longitude", "127.0559")
                        .param("radius", "1000")
                        .param("limit", "15"))
                .andExpect(status().isOk());

        verify(placeService).searchPlacesByKeyword(argThat(request ->
                "coffee".equals(request.getQuery())
                        && request.getLatitude().equals(37.5447)
                        && request.getLongitude().equals(127.0559)
                        && request.getRadius().equals(1000)
                        && request.getLimit().equals(15)
                        && request.getCategory() == null
        ));
    }
}
