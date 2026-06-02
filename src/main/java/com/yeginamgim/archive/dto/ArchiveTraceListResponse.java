package com.yeginamgim.archive.dto;

import com.yeginamgim.trace.dto.TraceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// 내가 남긴 흔적 목록 응답 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchiveTraceListResponse {
    private Long userId;

    @Builder.Default
    private List<TraceResponse> traces = new ArrayList<>();

    public static ArchiveTraceListResponse of(Long userId, List<TraceResponse> traces) {
        return ArchiveTraceListResponse.builder()
                .userId(userId)
                .traces(traces)
                .build();
    }
}
