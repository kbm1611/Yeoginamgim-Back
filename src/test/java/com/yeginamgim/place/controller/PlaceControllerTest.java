package com.yeginamgim.place.controller;

import com.yeginamgim.place.service.PlaceService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

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
        when(placeService.getPopularPlaces(10, "강남구")).thenReturn(List.of());

        mockMvc.perform(get("/api/places/popular")
                        .param("district", "강남구")
                        .param("limit", "10"))
                .andExpect(status().isOk());

        verify(placeService).getPopularPlaces(10, "강남구");
    }
}
