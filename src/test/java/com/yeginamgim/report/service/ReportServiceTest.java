package com.yeginamgim.report.service;

import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.notification.service.NotificationService;
import com.yeginamgim.report.dto.ReportCreateRequest;
import com.yeginamgim.report.entity.ReportEntity;
import com.yeginamgim.report.repository.ReportRepository;
import com.yeginamgim.trace.entity.Trace;
import com.yeginamgim.trace.enums.TraceStatus;
import com.yeginamgim.trace.repository.TraceRepository;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportServiceTest {

    private final ReportRepository reportRepository = mock(ReportRepository.class);
    private final TraceRepository traceRepository = mock(TraceRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final JWTService jwtService = mock(JWTService.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final ReportService reportService = new ReportService(
            reportRepository,
            traceRepository,
            userRepository,
            jwtService,
            notificationService
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
    void rejectsSelfReportWithForbidden() {
        UserEntity author = user(20L, "author@example.com");
        Trace trace = trace(10L, author, TraceStatus.ACTIVE);
        when(traceRepository.findById(10L)).thenReturn(Optional.of(trace));
        when(jwtService.getClaim("Bearer token")).thenReturn("author@example.com");
        when(userRepository.findByEmail("author@example.com")).thenReturn(Optional.of(author));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reportService.createReport(10L, "Bearer token", request("ABUSE")));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(reportRepository, never()).existsByUser_UserIdAndTrace_TraceId(any(), any());
        verify(reportRepository, never()).save(any());
    }

    @Test
    void rejectsReportForHiddenTraceWithConflict() {
        UserEntity author = user(20L, "author@example.com");
        UserEntity reporter = user(30L, "reporter@example.com");
        Trace trace = trace(10L, author, TraceStatus.HIDE);
        when(traceRepository.findById(10L)).thenReturn(Optional.of(trace));
        when(jwtService.getClaim("Bearer token")).thenReturn("reporter@example.com");
        when(userRepository.findByEmail("reporter@example.com")).thenReturn(Optional.of(reporter));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> reportService.createReport(10L, "Bearer token", request("ABUSE")));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(reportRepository, never()).existsByUser_UserIdAndTrace_TraceId(any(), any());
        verify(reportRepository, never()).save(any());
    }

    @Test
    void hidesTraceAndNotifiesUsersWhenReportCountReachesTen() {
        UserEntity author = user(20L, "author@example.com");
        UserEntity reporter = user(30L, "reporter@example.com");
        Trace trace = trace(10L, author, TraceStatus.ACTIVE);
        ReportEntity savedReport = ReportEntity.create(reporter, trace, "ABUSE");
        List<ReportEntity> reports = List.of(savedReport);

        when(traceRepository.findById(10L)).thenReturn(Optional.of(trace));
        when(jwtService.getClaim("Bearer token")).thenReturn("reporter@example.com");
        when(userRepository.findByEmail("reporter@example.com")).thenReturn(Optional.of(reporter));
        when(reportRepository.existsByUser_UserIdAndTrace_TraceId(30L, 10L)).thenReturn(false);
        when(reportRepository.save(any(ReportEntity.class))).thenReturn(savedReport);
        when(reportRepository.countByTrace_TraceId(10L)).thenReturn(10L);
        when(reportRepository.findByTrace_TraceId(10L)).thenReturn(reports);

        reportService.createReport(10L, "Bearer token", request("ABUSE"));

        assertThat(trace.getTraceStatus()).isEqualTo(TraceStatus.HIDE);
        assertThat(trace.getReportHiddenAt()).isNotNull();
        verify(notificationService).createTraceHiddenByReportNotifications(
                eq(trace),
                eq(reporter),
                eq(reports)
        );
    }

    @Test
    void doesNotHideTraceBeforeReportCountReachesTen() {
        UserEntity author = user(20L, "author@example.com");
        UserEntity reporter = user(30L, "reporter@example.com");
        Trace trace = trace(10L, author, TraceStatus.ACTIVE);
        ReportEntity savedReport = ReportEntity.create(reporter, trace, "ABUSE");

        when(traceRepository.findById(10L)).thenReturn(Optional.of(trace));
        when(jwtService.getClaim("Bearer token")).thenReturn("reporter@example.com");
        when(userRepository.findByEmail("reporter@example.com")).thenReturn(Optional.of(reporter));
        when(reportRepository.existsByUser_UserIdAndTrace_TraceId(30L, 10L)).thenReturn(false);
        when(reportRepository.save(any(ReportEntity.class))).thenReturn(savedReport);
        when(reportRepository.countByTrace_TraceId(10L)).thenReturn(9L);

        reportService.createReport(10L, "Bearer token", request("ABUSE"));

        assertThat(trace.getTraceStatus()).isEqualTo(TraceStatus.ACTIVE);
        assertThat(trace.getReportHiddenAt()).isNull();
        verify(notificationService, never()).createTraceHiddenByReportNotifications(any(), any(), any());
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
                .traceStatus(TraceStatus.ACTIVE)
                .build();
    }

    private Trace trace(Long traceId, UserEntity user, TraceStatus traceStatus) {
        return Trace.builder()
                .traceId(traceId)
                .user(user)
                .traceX(1)
                .traceY(2)
                .traceStatus(traceStatus)
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
