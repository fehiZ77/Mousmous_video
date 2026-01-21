package com.moustass.notification_service.service;

import com.moustass.notification_service.entity.Notification;
import com.moustass.notification_service.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotification(Notification notification){
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsForUser(Long userId){
        return notificationRepository.findByReceivedIdAndDateSeenAtIsNull(userId);
    }

    public List<Notification> getNotificationForUser(Long userId){
        return notificationRepository.findByReceivedIdOrderByIdDesc(userId);
    }

    @Transactional
    public void markAllAsSeen(Long userId){
        notificationRepository.markAllAsSeen(userId);
    }

    public Notification markAsSeen(Long id){
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setDateSeenAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }
}
