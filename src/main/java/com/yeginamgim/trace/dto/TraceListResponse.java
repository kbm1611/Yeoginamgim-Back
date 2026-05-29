package com.yeginamgim.trace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// 보드별 흔적 목록 응답 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TraceListResponse {
    private Long boardId;

    @Builder.Default
    private List<TraceResponse> traces = new ArrayList<>();
}
