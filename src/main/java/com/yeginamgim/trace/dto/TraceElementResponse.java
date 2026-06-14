package com.yeginamgim.trace.dto;

import com.yeginamgim.trace.entity.TraceElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

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
    private Instant createdAt;
    private Instant updatedAt;

    public static TraceElementResponse from(TraceElement element) {
        return TraceElementResponse.builder()
                .elementId(element.getElementId())
                .contentType(element.getContentType().name())
                .textContent(element.getTextContent())
                .imageUrl(element.getImageUrl())
                .elementX(element.getElementX())
                .elementY(element.getElementY())
                .styleJson(element.getStyleJson())
                .createdAt(element.getCreatedAt())
                .updatedAt(element.getUpdatedAt())
                .build();
    }
}
