package com.yeginamgim.archive.dto;

import com.yeginamgim.trace.dto.TraceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// 하루 단위로 묶은 흔적 응답 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchiveDateGroupResponse {
    private LocalDate date;
    private Integer traceCount;

    @Builder.Default
    private List<TraceResponse> traces = new ArrayList<>();
}
