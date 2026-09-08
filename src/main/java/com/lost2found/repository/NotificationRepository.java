package com.lost2found.repository;

import com.lost2found.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);
}
