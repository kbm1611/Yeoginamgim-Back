package com.yeginamgim.notification.controller;

import com.yeginamgim.notification.dto.NotificationResponse;
import com.yeginamgim.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponse> getNotifications(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return notificationService.getNotifications(authorization);
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationResponse markAsRead(
            @PathVariable Long notificationId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return notificationService.markAsRead(notificationId, authorization);
    }

    @PatchMapping("/read-all")
    public List<NotificationResponse> markAllAsRead(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return notificationService.markAllAsRead(authorization);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return Map.of("unreadCount", notificationService.countUnread(authorization));
    }
}
