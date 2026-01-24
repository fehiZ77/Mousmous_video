package com.moustass.notification_service.service;

import com.moustass.notification_service.client.users.UserClient;
import com.moustass.notification_service.dto.NotificationRequestDto;
import com.moustass.notification_service.dto.NotificationResponseDto;
import com.moustass.notification_service.entity.Action;
import com.moustass.notification_service.entity.Notification;
import com.moustass.notification_service.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserClient userClient;

    public NotificationService(NotificationRepository notificationRepository, UserClient userClient) {
        this.notificationRepository = notificationRepository;
        this.userClient = userClient;
    }

    public Notification createNotification(NotificationRequestDto dto){
        Notification notification = new Notification(
                dto.getOwnerId(),
                dto.getRecipeintId(),
                dto.getAction()
        );
        return notificationRepository.save(notification);
    }

    public List<NotificationResponseDto> getNotificationsForUser(Long userId, boolean isAll){
        List<Notification> notifications = isAll ? notificationRepository.findByReceivedIdOrderByIdDesc(userId) :
                notificationRepository.findByReceivedIdAndDateSeenAtIsNull(userId);
        List<NotificationResponseDto> result = new ArrayList<>();
        for(Notification notification : notifications){
            String owner = userClient.getUserName(notification.getTriggerId());
            String description = getDescription(owner, notification.getAction());
            String timeAgo = timeAgo(notification.getDateCreatedAt(), LocalDateTime.now());

            result.add(
                    new NotificationResponseDto(description, timeAgo)
            );
        }
        return result;
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

    // =========================== PRIVATE FUNCTION ===========================
    private String getDescription(String owner, Action action){
        String result = "";

        switch (action){
            case TRANSACTION_CREATED:
                result = "Nouvelle transaction créee par " + owner + " à vérifier.";
                break;
            case TRANSACTION_VERIFIED:
                result = "Transaction vérifiée par " + owner + ". Status : SUCCES";
                break;
            case TRANSACTION_VERIFIED_NOK:
                result = "Transaction vérifiée par " + owner + ". Status : FAILED";
                break;
        }

        return result;
    }

    private String timeAgo(LocalDateTime dateCreated, LocalDateTime dateNow) {

        Duration duration = Duration.between(dateCreated, dateNow);
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return "à l'instant";
        }

        long minutes = seconds / 60;
        if (minutes < 60) {
            return "il y a " + minutes + " min";
        }

        long hours = minutes / 60;
        if (hours < 24) {
            return "il y a " + hours + " heure" + (hours > 1 ? "s" : "");
        }

        long days = hours / 24;
        if (days < 30) {
            return "il y a " + days + " jour" + (days > 1 ? "s" : "");
        }

        long months = days / 30;
        if (months < 12) {
            return "il y a " + months + " mois";
        }

        long years = months / 12;
        return "il y a " + years + " an" + (years > 1 ? "s" : "");
    }
}
