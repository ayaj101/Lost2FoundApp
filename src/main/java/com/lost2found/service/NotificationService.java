package com.lost2found.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lost2found.model.Notification;
import com.lost2found.model.User;
import com.lost2found.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    public void createMatchNotification(User user, String message) {
        Notification notification = new Notification();
        notification.setUserId(user.getId());
        notification.setMessage(message);
        Notification saved = repository.saveAndFlush(notification);
        System.out.println("🔔 In-app notification saved: id=" + saved.getId()
            + ", userId=" + user.getId());
    }

    public List<Notification> unreadFor(User user) {
        return repository.findByUserIdAndReadFalseOrderByCreatedAtDesc(user.getId());
    }

    @Transactional
    public boolean markRead(Long id, User user) {
        return repository.findById(id)
                .filter(notification -> notification.getUserId().equals(user.getId()))
                .map(notification -> {
                    notification.setRead(true);
                    return true;
                })
                .orElse(false);
    }
}
