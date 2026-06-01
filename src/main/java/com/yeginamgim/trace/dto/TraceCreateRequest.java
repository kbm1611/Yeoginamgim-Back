package com.yeginamgim.trace.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

// 보드 위에 흔적을 생성할 때 사용하는 요청 DTO
@Data
public class TraceCreateRequest {
    private Integer traceX;
    private Integer traceY;
    private List<TraceElementCreateRequest> elements = new ArrayList<>();
}
