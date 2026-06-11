package com.yeginamgim.notification.service;

import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.follow.entity.Follow;
import com.yeginamgim.follow.repository.FollowRepository;
import com.yeginamgim.notification.dto.NotificationResponse;
import com.yeginamgim.notification.entity.Notification;
import com.yeginamgim.notification.repository.NotificationRepository;
import com.yeginamgim.trace.entity.Trace;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
    private final FollowRepository followRepository = mock(FollowRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final JWTService jwtService = mock(JWTService.class);
    private final NotificationService notificationService = new NotificationService(
            notificationRepository,
            followRepository,
            userRepository,
            jwtService
    );

    @Test
    void createFollowingTraceNotificationsCreatesNotificationsForFollowers() {
        UserEntity sender = user(2L, "sender@example.com", "sender");
        UserEntity follower = user(1L, "follower@example.com", "follower");
        Trace trace = Trace.builder()
                .traceId(10L)
                .user(sender)
                .traceX(1)
                .traceY(2)
                .build();
        Follow follow = Follow.builder()
                .follower(follower)
                .following(sender)
                .build();
        when(followRepository.findByFollowing_UserIdOrderByCreatedAtDesc(2L)).thenReturn(List.of(follow));

        notificationService.createFollowingTraceNotifications(sender, trace);

        verify(notificationRepository).saveAll(any());
    }

    @Test
    void getNotificationsReturnsCurrentUsersNotifications() {
        UserEntity receiver = user(1L, "receiver@example.com", "receiver");
        UserEntity sender = user(2L, "sender@example.com", "sender");
        Notification notification = Notification.builder()
                .notificationId(7L)
                .receiver(receiver)
                .sender(sender)
                .trace(Trace.builder().traceId(10L).traceX(1).traceY(2).build())
                .message("message")
                .notificationType(com.yeginamgim.notification.enums.NotificationType.FOLLOWING_TRACE_CREATED)
                .read(false)
                .build();
        when(jwtService.getClaim("Bearer token")).thenReturn("receiver@example.com");
        when(userRepository.findByEmail("receiver@example.com")).thenReturn(Optional.of(receiver));
        when(notificationRepository.findByReceiver_UserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(notification));

        List<NotificationResponse> responses = notificationService.getNotifications("Bearer token");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getNotificationId()).isEqualTo(7L);
    }

    @Test
    void markAsReadRejectsOtherUsersNotification() {
        UserEntity receiver = user(1L, "receiver@example.com", "receiver");
        when(jwtService.getClaim("Bearer token")).thenReturn("receiver@example.com");
        when(userRepository.findByEmail("receiver@example.com")).thenReturn(Optional.of(receiver));
        when(notificationRepository.findByNotificationIdAndReceiver_UserId(9L, 1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> notificationService.markAsRead(9L, "Bearer token"));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private UserEntity user(Long userId, String email, String nickname) {
        return UserEntity.builder()
                .userId(userId)
                .email(email)
                .nickname(nickname)
                .build();
    }
}
