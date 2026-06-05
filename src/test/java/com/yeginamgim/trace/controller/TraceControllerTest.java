package com.yeginamgim.trace.controller;

import com.yeginamgim.global.exception.FileUploadException;
import com.yeginamgim.global.exception.GlobalExceptionHandler;
import com.yeginamgim.trace.dto.TraceImageUploadResponse;
import com.yeginamgim.trace.service.TraceService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class TraceControllerTest {

    private final TraceService traceService = mock(TraceService.class);
    private final MockMvc mockMvc = standaloneSetup(new TraceController(traceService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void recentTracesAcceptPeriodDistrictAndLimitParameters() throws Exception {
        when(traceService.getRecentTraces("today", "Seongdong-gu", 5, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/traces/recent")
                        .param("period", "today")
                        .param("district", "Seongdong-gu")
                        .param("limit", "5"))
                .andExpect(status().isOk());

        verify(traceService).getRecentTraces("today", "Seongdong-gu", 5, null);
    }

    @Test
    void traceImageUploadReturnsImageUrlForAllowedImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "trace.png",
                "image/png",
                pngBytes()
        );
        when(traceService.uploadTraceImage(any(MultipartFile.class)))
                .thenReturn(TraceImageUploadResponse.of("/upload/board/trace.png"));

        mockMvc.perform(multipart("/api/traces/images").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("/upload/board/trace.png"));
    }

    @Test
    void traceImageUploadUnsupportedFileTypeReturnsStandardJsonBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "note.txt",
                "text/plain",
                "not an image".getBytes()
        );
        when(traceService.uploadTraceImage(any(MultipartFile.class)))
                .thenThrow(FileUploadException.unsupportedFileType());

        mockMvc.perform(multipart("/api/traces/images").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_FILE_TYPE"))
                .andExpect(jsonPath("$.message").value("Only JPEG, PNG, and WebP images can be uploaded."))
                .andExpect(jsonPath("$.status").value(400));
    }

    private byte[] pngBytes() {
        return new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47,
                0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D
        };
    }
}
