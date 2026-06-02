package com.yeginamgim.report.service;

import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.report.dto.ReportCreateRequest;
import com.yeginamgim.report.dto.ReportResponse;
import com.yeginamgim.report.entity.ReportEntity;
import com.yeginamgim.report.repository.ReportRepository;
import com.yeginamgim.trace.entity.Trace;
import com.yeginamgim.trace.repository.TraceRepository;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final TraceRepository traceRepository;
    private final UserRepository userRepository;
    private final JWTService jwtService;

    // trace_id 기준 흔적 신고 등록
    @Transactional
    public ReportResponse createReport(Long traceId, String authorization, ReportCreateRequest request) {
        validateCreateRequest(request);

        Trace trace = findTrace(traceId);
        UserEntity user = findUserByToken(authorization);

        if (reportRepository.existsByUser_UserIdAndTrace_TraceId(user.getUserId(), trace.getTraceId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 신고한 흔적입니다.");
        }

        ReportEntity report = reportRepository.save(ReportEntity.create(user, trace, request.getReportKind()));

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
