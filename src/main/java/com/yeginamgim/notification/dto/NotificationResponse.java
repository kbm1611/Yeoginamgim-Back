package com.yeginamgim.notification.dto;

import com.yeginamgim.notification.entity.Notification;
import com.yeginamgim.trace.entity.Trace;
import com.yeginamgim.user.entity.UserEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long notificationId;
    private String type;
    private String message;
    private Boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private Long senderUserId;
    private String senderNickname;
    private String senderProfileImageUrl;
    private Long boardId;
    private Long traceId;

    public static NotificationResponse from(Notification notification) {
        UserEntity sender = notification.getSender();
        Trace trace = notification.getTrace();

        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .type(notification.getNotificationType().name())
                .message(notification.getMessage())
                .read(notification.isRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .senderUserId(sender == null ? null : sender.getUserId())
                .senderNickname(sender == null ? null : sender.getNickname())
                .senderProfileImageUrl(sender == null ? null : sender.getProfileImageUrl())
                .boardId(trace == null || trace.getBoard() == null ? null : trace.getBoard().getBoardId())
                .traceId(trace == null ? null : trace.getTraceId())
                .build();
    }
}
