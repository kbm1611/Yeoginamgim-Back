package com.yeginamgim.trace.dto;

import com.yeginamgim.trace.enums.ContentType;
import lombok.Data;

// 흔적 요소 수정 요청 DTO
@Data
public class TraceElementUpdateRequest {
    private Long elementId;
    private ContentType contentType;
    private String textContent;
    private String imageUrl;
    private Integer elementX;
    private Integer elementY;
    private String styleJson;
}
