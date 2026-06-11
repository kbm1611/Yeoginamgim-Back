package com.yeginamgim.notification.entity;

import com.yeginamgim.notification.enums.NotificationType;
import com.yeginamgim.trace.entity.Trace;
import com.yeginamgim.user.entity.UserEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    @Test
    void createFollowingTraceNotificationStartsUnread() {
        UserEntity receiver = user(1L, "receiver@example.com");
        UserEntity sender = user(2L, "sender@example.com");
        Trace trace = Trace.builder()
                .traceId(10L)
                .user(sender)
                .traceX(30)
                .traceY(50)
                .build();

        Notification notification = Notification.createFollowingTraceCreated(
                receiver,
                sender,
                trace,
                "sender님이 새 흔적을 남겼습니다."
        );

        assertThat(notification.getReceiver()).isSameAs(receiver);
        assertThat(notification.getSender()).isSameAs(sender);
        assertThat(notification.getTrace()).isSameAs(trace);
        assertThat(notification.getNotificationType()).isEqualTo(NotificationType.FOLLOWING_TRACE_CREATED);
        assertThat(notification.getMessage()).isEqualTo("sender님이 새 흔적을 남겼습니다.");
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getReadAt()).isNull();
    }

    @Test
    void markAsReadStoresReadState() {
        Notification notification = Notification.createFollowingTraceCreated(
                user(1L, "receiver@example.com"),
                user(2L, "sender@example.com"),
                Trace.builder().traceId(10L).traceX(30).traceY(50).build(),
                "알림"
        );

        notification.markAsRead();

        assertThat(notification.isRead()).isTrue();
        assertThat(notification.getReadAt()).isNotNull();
    }

    private UserEntity user(Long userId, String email) {
        return UserEntity.builder()
                .userId(userId)
                .email(email)
                .nickname("user-" + userId)
                .build();
    }
}
