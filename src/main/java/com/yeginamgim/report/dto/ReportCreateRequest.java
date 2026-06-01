package com.yeginamgim.report.dto;

import lombok.Data;

// 흔적 신고 등록 요청 DTO
@Data
public class ReportCreateRequest {
    private Long userId;
    private String reportKind;
}
