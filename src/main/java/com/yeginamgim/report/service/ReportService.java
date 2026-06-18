package com.yeginamgim.report.service;

import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.notification.service.NotificationService;
import com.yeginamgim.report.dto.ReportCreateRequest;
import com.yeginamgim.report.dto.ReportResponse;
import com.yeginamgim.report.entity.ReportEntity;
import com.yeginamgim.report.repository.ReportRepository;
import com.yeginamgim.trace.entity.Trace;
import com.yeginamgim.trace.enums.TraceStatus;
import com.yeginamgim.trace.repository.TraceRepository;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final long REPORT_HIDE_THRESHOLD = 10L;

    private final ReportRepository reportRepository;
    private final TraceRepository traceRepository;
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final NotificationService notificationService;

    // trace_id 기준 흔적 신고 등록
    @Transactional
    public ReportResponse createReport(Long traceId, String authorization, ReportCreateRequest request) {
        validateCreateRequest(request);

        Trace trace = findTrace(traceId);
        UserEntity user = findUserByToken(authorization);
        validateReporterCanReport(trace, user);

        if (reportRepository.existsByUser_UserIdAndTrace_TraceId(user.getUserId(), trace.getTraceId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 신고한 흔적입니다.");
        }

        ReportEntity report = reportRepository.save(ReportEntity.create(user, trace, request.getReportKind()));
        hideTraceAndNotifyIfThresholdReached(trace, user);

        return ReportResponse.from(report);
    }

    private void validateCreateRequest(ReportCreateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "신고 요청은 필수입니다.");
        }

        if (request.getReportKind() == null || request.getReportKind().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "신고 사유는 필수입니다.");
        }
    }

    private void validateReporterCanReport(Trace trace, UserEntity user) {
        if (trace.getUser() != null && trace.getUser().getUserId() != null
                && trace.getUser().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 작성한 흔적은 신고할 수 없습니다.");
        }

        if (trace.getTraceStatus() != TraceStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 숨김 처리된 흔적은 신고할 수 없습니다.");
        }
    }

    private void hideTraceAndNotifyIfThresholdReached(Trace trace, UserEntity reporter) {
        long reportCount = reportRepository.countByTrace_TraceId(trace.getTraceId());
        if (reportCount < REPORT_HIDE_THRESHOLD) {
            return;
        }

        trace.hideByReport(Instant.now());

        List<ReportEntity> reports = reportRepository.findByTrace_TraceId(trace.getTraceId());
        notificationService.createTraceHiddenByReportNotifications(trace, reporter, reports);
    }

    private Trace findTrace(Long traceId) {
        return traceRepository.findById(traceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "흔적을 찾을 수 없습니다."));
    }

    private UserEntity findUserByToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 요청입니다.");
        }

        String email = jwtService.getClaim(authorization);
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 요청입니다.");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

}
