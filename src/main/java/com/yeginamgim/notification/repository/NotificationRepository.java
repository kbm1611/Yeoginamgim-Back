package com.yeginamgim.notification.repository;

import com.yeginamgim.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByReceiver_UserIdOrderByCreatedAtDesc(Long receiverId);

    List<Notification> findByReceiver_UserIdAndReadFalseOrderByCreatedAtDesc(Long receiverId);

    Optional<Notification> findByNotificationIdAndReceiver_UserId(Long notificationId, Long receiverId);

    long countByReceiver_UserIdAndReadFalse(Long receiverId);
}
