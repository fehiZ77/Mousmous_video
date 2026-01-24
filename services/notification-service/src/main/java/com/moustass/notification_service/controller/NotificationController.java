package com.moustass.notification_service.controller;

import com.moustass.notification_service.dto.NotificationRequestDto;
import com.moustass.notification_service.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllNotificationsForUser(@RequestParam Long userId){
        try {
            return new ResponseEntity<>(notificationService.getNotificationsForUser(userId, true), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("/not-seen")
    public ResponseEntity<?> getNotificationsForUser(@RequestParam Long userId){
        try {
            return new ResponseEntity<>(notificationService.getNotificationsForUser(userId, false), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PutMapping("/mark-all-seen")
    public ResponseEntity<String> markAllAsSeen(@RequestParam Long userId){
        try {
            notificationService.markAllAsSeen(userId);
            return new ResponseEntity<>("Seen all notifications", HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Object>  create(@RequestBody NotificationRequestDto notification){
        try {
            return new ResponseEntity<>(notificationService.createNotification(notification), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
