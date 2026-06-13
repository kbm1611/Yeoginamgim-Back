package com.yeginamgim.notification.controller;

import com.yeginamgim.notification.dto.NotificationResponse;
import com.yeginamgim.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class NotificationControllerTest {

    private final NotificationService notificationService = mock(NotificationService.class);
    private final MockMvc mockMvc = standaloneSetup(new NotificationController(notificationService)).build();

    @Test
    void notificationListEndpointDelegatesAuthorizationHeader() throws Exception {
        when(notificationService.getNotifications("Bearer token")).thenReturn(List.of(notificationResponse(false)));

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].notificationId").value(7))
                .andExpect(jsonPath("$[0].read").value(false))
                .andExpect(jsonPath("$[0].traceId").value(10));

        verify(notificationService).getNotifications("Bearer token");
    }

    @Test
    void markAsReadEndpointDelegatesAuthorizationHeader() throws Exception {
        when(notificationService.markAsRead(7L, "Bearer token")).thenReturn(notificationResponse(true));

        mockMvc.perform(patch("/api/notifications/7/read")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value(7))
                .andExpect(jsonPath("$.read").value(true));

        verify(notificationService).markAsRead(7L, "Bearer token");
    }

    @Test
    void unreadCountEndpointReturnsJsonObject() throws Exception {
        when(notificationService.countUnread("Bearer token")).thenReturn(3L);

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(3));

        verify(notificationService).countUnread("Bearer token");
    }

    private NotificationResponse notificationResponse(boolean read) {
        return NotificationResponse.builder()
                .notificationId(7L)
                .type("FOLLOWING_TRACE_CREATED")
                .message("message")
                .read(read)
                .createdAt(Instant.parse("2026-06-11T15:00:00Z"))
                .senderUserId(2L)
                .senderNickname("sender")
                .traceId(10L)
                .build();
    }
}
