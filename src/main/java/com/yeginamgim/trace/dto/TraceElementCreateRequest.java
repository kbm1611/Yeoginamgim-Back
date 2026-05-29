package com.yeginamgim.trace.dto;

import com.yeginamgim.trace.enums.ContentType;
import lombok.Data;

// 흔적 안에 들어가는 포스트잇/폴라로이드 요소 생성 요청
@Data
public class TraceElementCreateRequest {
    private ContentType contentType;
    private String textContent;
    private String imageUrl;
    private Integer elementX;
    private Integer elementY;
    private String styleJson;
}
