package com.yeginamgim.report.service;

import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.report.dto.ReportCreateRequest;
import com.yeginamgim.report.repository.ReportRepository;
import com.yeginamgim.trace.entity.Trace;
import com.yeginamgim.trace.repository.TraceRepository;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportServiceTest {

    private final ReportRepository reportRepository = mock(ReportRepository.class);
    private final TraceRepository traceRepository = mock(TraceRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final JWTService jwtService = mock(JWTService.class);
    private final ReportService reportService = new ReportService(
            reportRepository,
            traceRepository,
            userRepository,
            jwtService
    );

    @Test
    void rejectsDuplicateReportWithConflict() {
        Trace trace = trace(10L);
        UserEntity user = user(20L, "reporter@example.com");
        when(traceRepository.findById(10L)).thenReturn(Optional.of(trace));
        when(jwtService.getClaim("Bearer token")).thenReturn("reporter@example.com");
        when(userRepository.findByEmail("reporter@example.com")).thenReturn(Optional.of(user));
        when(reportRepository.existsByUser_UserIdAndTrace_TraceId(20L, 10L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reportService.createReport(10L, "Bearer token", request("ABUSE")));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(reportRepository, never()).save(any());
    }

    @Test
    void rejectsBlankReportKindWithBadRequest() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reportService.createReport(10L, "Bearer token", request("   ")));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(traceRepository, never()).findById(any());
        verify(reportRepository, never()).save(any());
    }

    @Test
    void rejectsMissingAuthorizationWithUnauthorized() {
        when(traceRepository.findById(10L)).thenReturn(Optional.of(trace(10L)));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reportService.createReport(10L, null, request("SPAM")));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(reportRepository, never()).save(any());
    }

    @Test
    void rejectsMissingTraceWithNotFound() {
        when(traceRepository.findById(404L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reportService.createReport(404L, "Bearer token", request("PRIVACY")));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(jwtService, never()).getClaim(any());
        verify(reportRepository, never()).save(any());
    }

    private ReportCreateRequest request(String reportKind) {
        ReportCreateRequest request = new ReportCreateRequest();
        request.setReportKind(reportKind);
        return request;
    }

    private Trace trace(Long traceId) {
        return Trace.builder()
                .traceId(traceId)
                .traceX(1)
                .traceY(2)
                .build();
    }

    private UserEntity user(Long userId, String email) {
        return UserEntity.builder()
                .userId(userId)
                .email(email)
                .nickname("reporter")
                .build();
    }
}
