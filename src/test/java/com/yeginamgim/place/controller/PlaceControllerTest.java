package com.yeginamgim.place.controller;

import com.yeginamgim.place.service.PlaceService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.mockito.Mockito.mock;

class PlaceControllerTest {

    private final PlaceService placeService = mock(PlaceService.class);
    private final MockMvc mockMvc = standaloneSetup(new PlaceController(placeService)).build();

    @Test
    void doesNotExposeSinglePlaceLookupByKakaoPlaceId() throws Exception {
        mockMvc.perform(get("/api/places/cache-board"))
                .andExpect(status().isNotFound());
    }
}
