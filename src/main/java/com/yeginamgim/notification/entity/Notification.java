package com.yeginamgim.notification.entity;

import com.yeginamgim.notification.enums.NotificationType;
import com.yeginamgim.trace.entity.Trace;
import com.yeginamgim.user.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "notification",
        indexes = {
                @Index(name = "idx_notification_receiver_read_created", columnList = "receiver_id, is_read, created_at"),
                @Index(name = "idx_notification_receiver_created", columnList = "receiver_id, created_at"),
                @Index(name = "idx_notification_trace_id", columnList = "trace_id")
        }
)
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    // User who receives the notification.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserEntity receiver;

    // User who triggered the notification.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private UserEntity sender;

    // Trace connected to the notification.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trace_id")
    private Trace trace;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;

    @Column(name = "message", nullable = false, length = 255)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static Notification createFollowingTraceCreated(
            UserEntity receiver,
            UserEntity sender,
            Trace trace,
            String message
    ) {
        return Notification.builder()
                .receiver(receiver)
                .sender(sender)
                .trace(trace)
                .notificationType(NotificationType.FOLLOWING_TRACE_CREATED)
                .message(message)
                .read(false)
                .build();
    }

    public void markAsRead() {
        if (this.read) {
            return;
        }

        this.read = true;
        this.readAt = Instant.now();
    }

    @PrePersist
    private void prePersist() {
        this.createdAt = Instant.now();
    }
}
