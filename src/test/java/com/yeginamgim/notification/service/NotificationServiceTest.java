package com.yeginamgim.notification.service;

import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.customboard.entity.CustomBoard;
import com.yeginamgim.customboard.entity.CustomBoardMember;
import com.yeginamgim.customboard.enums.BoardRole;
import com.yeginamgim.customboard.repository.CustomBoardMemberRepository;
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
    private final CustomBoardMemberRepository customBoardMemberRepository = mock(CustomBoardMemberRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final JWTService jwtService = mock(JWTService.class);
    private final NotificationService notificationService = new NotificationService(
            notificationRepository,
            followRepository,
            customBoardMemberRepository,
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
    void createCustomBoardTraceNotificationsCreatesNotificationsOnlyForBoardMembers() {
        UserEntity sender = user(2L, "sender@example.com", "sender");
        UserEntity member = user(1L, "member@example.com", "member");
        UserEntity outsideFollower = user(3L, "outside@example.com", "outside");
        CustomBoard customBoard = CustomBoard.builder()
                .customBoardId(33L)
                .user(sender)
                .boardTitle("trip")
                .build();
        Trace trace = Trace.builder()
                .traceId(10L)
                .customBoard(customBoard)
                .user(sender)
                .traceX(1)
                .traceY(2)
                .build();
        CustomBoardMember senderMember = CustomBoardMember.builder()
                .customBoard(customBoard)
                .user(sender)
                .role(BoardRole.OWNER)
                .build();
        CustomBoardMember boardMember = CustomBoardMember.builder()
                .customBoard(customBoard)
                .user(member)
                .role(BoardRole.MEMBER)
                .build();
        Follow outsideFollow = Follow.builder()
                .follower(outsideFollower)
                .following(sender)
                .build();

        when(customBoardMemberRepository.findByCustomBoard_CustomBoardIdOrderByCreatedAtAsc(33L))
                .thenReturn(List.of(senderMember, boardMember));
        when(followRepository.findByFollowing_UserIdOrderByCreatedAtDesc(2L)).thenReturn(List.of(outsideFollow));

        notificationService.createCustomBoardTraceNotifications(sender, trace);

        verify(notificationRepository).saveAll(org.mockito.ArgumentMatchers.argThat(notifications -> {
            List<Notification> notificationList = new java.util.ArrayList<>();
            notifications.forEach(notificationList::add);
            return notificationList.size() == 1
                    && notificationList.get(0).getReceiver().getUserId().equals(1L);
        }));
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
