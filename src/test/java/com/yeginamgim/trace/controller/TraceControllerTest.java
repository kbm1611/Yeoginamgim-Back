package com.yeginamgim.trace.controller;

import com.yeginamgim.trace.service.TraceService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class TraceControllerTest {

    private final TraceService traceService = mock(TraceService.class);
    private final MockMvc mockMvc = standaloneSetup(new TraceController(traceService)).build();

    @Test
    void recentTracesAcceptPeriodDistrictAndLimitParameters() throws Exception {
        when(traceService.getRecentTraces("today", "성동구", 5, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/traces/recent")
                        .param("period", "today")
                        .param("district", "성동구")
                        .param("limit", "5"))
                .andExpect(status().isOk());

        verify(traceService).getRecentTraces("today", "성동구", 5, null);
    }
}
