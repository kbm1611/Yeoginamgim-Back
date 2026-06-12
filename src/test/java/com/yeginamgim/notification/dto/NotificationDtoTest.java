package com.yeginamgim.notification.dto;

import com.yeginamgim.notification.entity.Notification;
import com.yeginamgim.notification.enums.NotificationType;
import com.yeginamgim.board.entity.BoardEntity;
import com.yeginamgim.customboard.entity.CustomBoard;
import com.yeginamgim.trace.entity.Trace;
import com.yeginamgim.user.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationDtoTest {

    @Test
    void notificationResponseContainsNotificationAndNavigationFields() {
        UserEntity sender = UserEntity.builder()
                .userId(2L)
                .email("sender@example.com")
                .nickname("sender")
                .profileImageUrl("https://image.example/sender.png")
                .build();
        Trace trace = Trace.builder()
                .traceId(10L)
                .board(BoardEntity.builder().boardId(3L).kakaoPlaceId("place-1").build())
                .user(sender)
                .traceX(1)
                .traceY(2)
                .build();
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 11, 12, 0);
        Notification notification = Notification.builder()
                .notificationId(7L)
                .sender(sender)
                .trace(trace)
                .notificationType(NotificationType.FOLLOWING_TRACE_CREATED)
                .message("sender님이 새 흔적을 남겼습니다.")
                .read(false)
                .createdAt(createdAt)
                .build();

        NotificationResponse response = NotificationResponse.from(notification);

        assertThat(response.getNotificationId()).isEqualTo(7L);
        assertThat(response.getType()).isEqualTo("FOLLOWING_TRACE_CREATED");
        assertThat(response.getMessage()).isEqualTo("sender님이 새 흔적을 남겼습니다.");
        assertThat(response.getRead()).isFalse();
        assertThat(response.getReadAt()).isNull();
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
        assertThat(response.getSenderUserId()).isEqualTo(2L);
        assertThat(response.getSenderNickname()).isEqualTo("sender");
        assertThat(response.getSenderProfileImageUrl()).isEqualTo("https://image.example/sender.png");
        assertThat(response.getTraceId()).isEqualTo(10L);
        assertThat(response.getBoardId()).isEqualTo(3L);
    }

    @Test
    void notificationResponseUsesCustomBoardIdWhenTraceBelongsToCustomBoard() {
        UserEntity sender = UserEntity.builder()
                .userId(2L)
                .email("sender@example.com")
                .nickname("sender")
                .build();
        CustomBoard customBoard = CustomBoard.builder()
                .customBoardId(33L)
                .user(sender)
                .boardTitle("custom")
                .build();
        Trace trace = Trace.builder()
                .traceId(10L)
                .customBoard(customBoard)
                .user(sender)
                .traceX(1)
                .traceY(2)
                .build();
        Notification notification = Notification.builder()
                .notificationId(7L)
                .sender(sender)
                .trace(trace)
                .notificationType(NotificationType.FOLLOWING_TRACE_CREATED)
                .message("sender")
                .read(false)
                .createdAt(LocalDateTime.of(2026, 6, 11, 12, 0))
                .build();

        NotificationResponse response = NotificationResponse.from(notification);

        assertThat(response.getTraceId()).isEqualTo(10L);
        assertThat(response.getBoardId()).isEqualTo(33L);
    }
}
