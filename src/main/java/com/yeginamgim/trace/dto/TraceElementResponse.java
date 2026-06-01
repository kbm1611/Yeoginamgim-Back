package com.yeginamgim.trace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 흔적 요소 응답 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TraceElementResponse {
    private Long elementId;
    private String contentType;
    private String textContent;
    private String imageUrl;
    private Integer elementX;
    private Integer elementY;
    private String styleJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
