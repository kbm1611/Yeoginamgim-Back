package com.yeginamgim.report.dto;

import com.yeginamgim.report.entity.ReportEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 흔적 신고 등록 응답 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {
    private Long reportId;
    private Long traceId;
    private Long userId;
    private String reportKind;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReportResponse from(ReportEntity report) {
        return ReportResponse.builder()
                .reportId(report.getReportId())
                .traceId(report.getTrace().getTraceId())
                .userId(report.getUser().getUserId())
                .reportKind(report.getReportKind())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
