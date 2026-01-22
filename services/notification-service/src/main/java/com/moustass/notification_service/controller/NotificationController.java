package com.moustass.notification_service.controller;

import com.moustass.notification_service.entity.Notification;
import com.moustass.notification_service.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/user/{userId}")
    public List<Notification> getNotificationsForUser(@PathVariable Long userId){
        return notificationService.getNotificationsForUser(userId);
    }

    @GetMapping("/user/{userId}/all")
    public List<Notification> getAllNotificationsForUser(@PathVariable Long userId){
        return notificationService.getNotificationForUser(userId);
    }

    @PutMapping("/user/{userId}/mark-all-seen")
    public ResponseEntity<Void> markAllAsSeen(@PathVariable Long userId){
        notificationService.markAllAsSeen(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/create")
    public Notification create(@RequestBody Notification notification){
        notification.setDateSeenAt(null);
        return notificationService.createNotification(notification);
    }

    @PutMapping("/{id}/seen")
    public ResponseEntity<Notification> markAsSeen(@PathVariable Long id){
       Notification updated = notificationService.markAsSeen(id);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/test")
    public String test() {
        return "API OK";
    }
}
