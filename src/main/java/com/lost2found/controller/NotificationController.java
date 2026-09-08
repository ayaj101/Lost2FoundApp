package com.lost2found.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lost2found.model.User;
import com.lost2found.repository.UserRepository;
import com.lost2found.service.NotificationService;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public NotificationController(UserRepository userRepository, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id, Principal principal) {
        return update(id, principal);
    }

    @PostMapping("/{id}/ignore")
    public ResponseEntity<Void> ignore(@PathVariable Long id, Principal principal) {
        return update(id, principal);
    }

    private ResponseEntity<Void> update(Long id, Principal principal) {
        User user = userRepository.findByUsernameIgnoreCase(principal.getName()).orElse(null);
        if (user == null || !notificationService.markRead(id, user)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
