package com.yeginamgim.trace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 흔적 이미지 업로드 응답 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TraceImageUploadResponse {
    private String imageUrl;

    public static TraceImageUploadResponse of(String imageUrl) {
        return TraceImageUploadResponse.builder()
                .imageUrl(imageUrl)
                .build();
    }
}
