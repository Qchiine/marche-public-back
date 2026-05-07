package com.emsi.marches_backend.controller;

import com.emsi.marches_backend.model.NotificationDocument;
import com.emsi.marches_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<java.util.List<NotificationDocument>> getNotifications(Authentication authentication) {
        String email = authentication.getName();
        var notifications = notificationService.getUserNotifications(email);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/count-non-lues")
    public ResponseEntity<Map<String, Long>> countUnreadNotifications(Authentication authentication) {
        String email = authentication.getName();
        long count = notificationService.countUnreadNotifications(email);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/lire")
    public ResponseEntity<NotificationDocument> markAsRead(@PathVariable String id, Authentication authentication) {
        NotificationDocument notification = notificationService.markAsRead(authentication.getName(), id);
        return ResponseEntity.ok(notification);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable String id, Authentication authentication) {
        notificationService.deleteNotification(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
