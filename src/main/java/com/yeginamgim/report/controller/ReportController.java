package com.yeginamgim.report.controller;

import com.yeginamgim.report.dto.ReportCreateRequest;
import com.yeginamgim.report.dto.ReportResponse;
import com.yeginamgim.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// 악성 글, 욕설 신고 기능
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReportController {

    private final ReportService reportService;

    // trace_id 기준 흔적 신고 등록 및 신고 사유 저장
    @PostMapping("/traces/{traceId}/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public ReportResponse createReport(
            @PathVariable Long traceId,
            @RequestBody ReportCreateRequest request
    ) {
        return reportService.createReport(traceId, request);
    }
}
