package com.yeginamgim.trace.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

// 흔적 수정 요청 DTO
@Data
public class TraceUpdateRequest {
    private Integer traceX;
    private Integer traceY;
    private List<TraceElementUpdateRequest> elements = new ArrayList<>();
}
