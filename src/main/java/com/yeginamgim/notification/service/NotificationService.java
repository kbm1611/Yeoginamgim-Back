package com.yeginamgim.notification.service;

import com.yeginamgim.board.dto.PlaceInfo;
import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.customboard.entity.CustomBoardMember;
import com.yeginamgim.customboard.repository.CustomBoardMemberRepository;
import com.yeginamgim.follow.entity.Follow;
import com.yeginamgim.follow.repository.FollowRepository;
import com.yeginamgim.notification.dto.NotificationResponse;
import com.yeginamgim.notification.entity.Notification;
import com.yeginamgim.notification.enums.NotificationType;
import com.yeginamgim.notification.repository.NotificationRepository;
import com.yeginamgim.place.repository.PlaceCsvStore;
import com.yeginamgim.report.entity.ReportEntity;
import com.yeginamgim.trace.entity.Trace;
import com.yeginamgim.trace.entity.TraceElement;
import com.yeginamgim.trace.repository.TraceElementRepository;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final FollowRepository followRepository;
    private final CustomBoardMemberRepository customBoardMemberRepository;
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final TraceElementRepository traceElementRepository;
    private final PlaceCsvStore placeCsvStore;

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

    @Transactional
    public void createTraceHiddenByReportNotifications(
            Trace trace,
            UserEntity thresholdReporter,
            List<ReportEntity> reports
    ) {
        if (trace == null || trace.getUser() == null || thresholdReporter == null) {
            return;
        }

        UserEntity author = trace.getUser();
        String boardLabel = resolveBoardLabel(trace);
        String tracePreview = resolveTracePreview(trace);

        List<Notification> notifications = new ArrayList<>();
        notifications.add(Notification.createTraceHiddenByReportForAuthor(
                author,
                thresholdReporter,
                trace,
                boardLabel + "에 작성한 \"" + tracePreview + "\" 흔적이 여러 사용자에게 신고되어 숨김 처리되었습니다."
        ));

        findReporters(reports).forEach(reporter -> notifications.add(
                Notification.createTraceHiddenByReportForReporter(
                        reporter,
                        author,
                        trace,
                        "신고한 " + boardLabel + "의 \"" + tracePreview + "\" 흔적이 숨김 처리되었습니다."
                )
        ));

        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(String authorization) {
        UserEntity receiver = findUserByToken(authorization);

        List<Notification> notifications = notificationRepository.findByReceiver_UserIdOrderByCreatedAtDesc(receiver.getUserId());
        return toNotificationResponses(notifications);
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, String authorization) {
        UserEntity receiver = findUserByToken(authorization);
        Notification notification = notificationRepository
                .findByNotificationIdAndReceiver_UserId(notificationId, receiver.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found."));

        notification.markAsRead();

        return toNotificationResponse(notification, findTraceElementMap(List.of(notification)));
    }

    @Transactional
    public List<NotificationResponse> markAllAsRead(String authorization) {
        UserEntity receiver = findUserByToken(authorization);
        List<Notification> notifications = notificationRepository
                .findByReceiver_UserIdAndReadFalseOrderByCreatedAtDesc(receiver.getUserId());

        notifications.forEach(Notification::markAsRead);

        return toNotificationResponses(notifications);
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

    private List<NotificationResponse> toNotificationResponses(List<Notification> notifications) {
        Map<Long, List<TraceElement>> traceElementMap = findTraceElementMap(notifications);

        return notifications.stream()
                .map(notification -> toNotificationResponse(notification, traceElementMap))
                .toList();
    }

    private NotificationResponse toNotificationResponse(
            Notification notification,
            Map<Long, List<TraceElement>> traceElementMap
    ) {
        Trace trace = notification.getTrace();
        String placeName = resolvePlaceName(trace);
        String boardTitle = resolveBoardTitle(trace);
        String tracePreview = resolveTracePreview(trace, traceElementMap);
        String displayMessage = resolveDisplayMessage(notification, placeName, boardTitle);

        return NotificationResponse.from(
                notification,
                displayMessage,
                placeName,
                boardTitle,
                tracePreview
        );
    }

    private Map<Long, List<TraceElement>> findTraceElementMap(List<Notification> notifications) {
        List<Long> traceIds = notifications.stream()
                .map(Notification::getTrace)
                .filter(trace -> trace != null && trace.getTraceId() != null)
                .map(Trace::getTraceId)
                .distinct()
                .toList();

        if (traceIds.isEmpty()) {
            return Map.of();
        }

        return traceElementRepository.findByTrace_TraceIdInOrderByElementIdAsc(traceIds)
                .stream()
                .collect(Collectors.groupingBy(element -> element.getTrace().getTraceId()));
    }

    private String resolveDisplayMessage(Notification notification, String placeName, String boardTitle) {
        if (notification.getNotificationType() != NotificationType.FOLLOWING_TRACE_CREATED) {
            return notification.getMessage();
        }

        String senderNickname = notification.getSender() == null ? null : notification.getSender().getNickname();
        if (!StringUtils.hasText(senderNickname)) {
            return notification.getMessage();
        }

        if (StringUtils.hasText(placeName)) {
            return senderNickname + "님이 " + placeName + "에 새 흔적을 남겼습니다.";
        }

        if (StringUtils.hasText(boardTitle)) {
            return senderNickname + "님이 " + boardTitle + "에 새 흔적을 남겼습니다.";
        }

        return notification.getMessage();
    }

    private List<UserEntity> findReporters(List<ReportEntity> reports) {
        if (reports == null || reports.isEmpty()) {
            return List.of();
        }

        Map<Long, UserEntity> reporterMap = new LinkedHashMap<>();
        for (ReportEntity report : reports) {
            if (report == null || report.getUser() == null || report.getUser().getUserId() == null) {
                continue;
            }
            reporterMap.putIfAbsent(report.getUser().getUserId(), report.getUser());
        }
        return List.copyOf(reporterMap.values());
    }

    private String resolveBoardLabel(Trace trace) {
        String placeName = resolvePlaceName(trace);
        if (StringUtils.hasText(placeName)) {
            return placeName;
        }

        String boardTitle = resolveBoardTitle(trace);
        if (StringUtils.hasText(boardTitle)) {
            return boardTitle;
        }

        return "보드";
    }

    private String resolveTracePreview(Trace trace) {
        if (trace == null || trace.getTraceId() == null) {
            return "새 흔적";
        }

        List<TraceElement> elements = traceElementRepository.findByTrace_TraceIdOrderByElementIdAsc(trace.getTraceId());
        if (elements == null || elements.isEmpty()) {
            return "이미지 흔적";
        }

        return elements.stream()
                .map(TraceElement::getTextContent)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .findFirst()
                .orElse("이미지 흔적");
    }

    private String resolvePlaceName(Trace trace) {
        if (trace == null || trace.getBoard() == null) {
            return null;
        }

        return placeCsvStore.findByKakaoPlaceId(trace.getBoard().getKakaoPlaceId())
                .map(PlaceInfo::getPlaceName)
                .filter(StringUtils::hasText)
                .orElse(null);
    }

    private String resolveBoardTitle(Trace trace) {
        if (trace == null || trace.getCustomBoard() == null) {
            return null;
        }

        String boardTitle = trace.getCustomBoard().getBoardTitle();
        return StringUtils.hasText(boardTitle) ? boardTitle : null;
    }

    private String resolveTracePreview(Trace trace, Map<Long, List<TraceElement>> traceElementMap) {
        if (trace == null || trace.getTraceId() == null) {
            return null;
        }

        List<TraceElement> elements = traceElementMap.getOrDefault(trace.getTraceId(), List.of());
        return elements.stream()
                .map(TraceElement::getTextContent)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .findFirst()
                .orElse("새 흔적");
    }
}
