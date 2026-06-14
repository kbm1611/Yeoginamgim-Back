package com.yeginamgim.notification.service;

import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.customboard.entity.CustomBoardMember;
import com.yeginamgim.customboard.repository.CustomBoardMemberRepository;
import com.yeginamgim.follow.entity.Follow;
import com.yeginamgim.follow.repository.FollowRepository;
import com.yeginamgim.notification.dto.NotificationResponse;
import com.yeginamgim.notification.entity.Notification;
import com.yeginamgim.notification.repository.NotificationRepository;
import com.yeginamgim.trace.entity.Trace;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final FollowRepository followRepository;
    private final CustomBoardMemberRepository customBoardMemberRepository;
    private final UserRepository userRepository;
    private final JWTService jwtService;

    @Transactional
    public void createFollowingTraceNotifications(UserEntity sender, Trace trace) {
        List<Notification> notifications = followRepository
                .findByFollowing_UserIdOrderByCreatedAtDesc(sender.getUserId())
                .stream()
                .map(Follow::getFollower)
                .filter(receiver -> !receiver.getUserId().equals(sender.getUserId()))
                .map(receiver -> Notification.createFollowingTraceCreated(
                        receiver,
                        sender,
                        trace,
                        sender.getNickname() + "님이 새 흔적을 남겼습니다."
                ))
                .toList();

        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
    }

    @Transactional
    public void createCustomBoardTraceNotifications(UserEntity sender, Trace trace) {
        if (trace.getCustomBoard() == null) {
            return;
        }

        Long customBoardId = trace.getCustomBoard().getCustomBoardId();
        List<Notification> notifications = customBoardMemberRepository
                .findByCustomBoard_CustomBoardIdOrderByCreatedAtAsc(customBoardId)
                .stream()
                .map(CustomBoardMember::getUser)
                .filter(receiver -> !receiver.getUserId().equals(sender.getUserId()))
                .map(receiver -> Notification.createFollowingTraceCreated(
                        receiver,
                        sender,
                        trace,
                        sender.getNickname() + "님이 커스텀 보드에 새 흔적을 남겼습니다."
                ))
                .toList();

        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(String authorization) {
        UserEntity receiver = findUserByToken(authorization);

        return notificationRepository.findByReceiver_UserIdOrderByCreatedAtDesc(receiver.getUserId())
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, String authorization) {
        UserEntity receiver = findUserByToken(authorization);
        Notification notification = notificationRepository
                .findByNotificationIdAndReceiver_UserId(notificationId, receiver.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found."));

        notification.markAsRead();

        return NotificationResponse.from(notification);
    }

    @Transactional
    public List<NotificationResponse> markAllAsRead(String authorization) {
        UserEntity receiver = findUserByToken(authorization);
        List<Notification> notifications = notificationRepository
                .findByReceiver_UserIdAndReadFalseOrderByCreatedAtDesc(receiver.getUserId());

        notifications.forEach(Notification::markAsRead);

        return notifications.stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countUnread(String authorization) {
        UserEntity receiver = findUserByToken(authorization);
        return notificationRepository.countByReceiver_UserIdAndReadFalse(receiver.getUserId());
    }

    private UserEntity findUserByToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized request.");
        }

        String email = jwtService.getClaim(authorization);
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized request.");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
    }
}
