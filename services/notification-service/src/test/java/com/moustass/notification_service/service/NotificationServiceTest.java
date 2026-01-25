package com.moustass.notification_service.service;

import com.moustass.notification_service.client.users.UserClient;
import com.moustass.notification_service.dto.NotificationRequestDto;
import com.moustass.notification_service.dto.NotificationResponseDto;
import com.moustass.notification_service.entity.Action;
import com.moustass.notification_service.entity.Notification;
import com.moustass.notification_service.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private NotificationService notificationService;

    private Notification testNotification;
    private NotificationRequestDto notificationRequestDto;

    @BeforeEach
    void setUp() {
        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setTriggerId(1L);
        testNotification.setReceivedId(2L);
        testNotification.setAction(Action.TRANSACTION_CREATED);
        testNotification.setDateCreatedAt(LocalDateTime.now().minusMinutes(5));
        testNotification.setDateSeenAt(null);

        notificationRequestDto = new NotificationRequestDto();
        notificationRequestDto.setOwnerId(1L);
        notificationRequestDto.setRecipeintId(2L);
        notificationRequestDto.setAction(Action.TRANSACTION_CREATED.name());
    }

    @Test
    void testCreateNotification_Success() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        Notification result = notificationService.createNotification(notificationRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testGetNotificationsForUser_AllNotifications() {
        // Arrange
        List<Notification> notifications = new ArrayList<>();
        notifications.add(testNotification);

        when(notificationRepository.findByReceivedIdOrderByIdDesc(2L)).thenReturn(notifications);
        when(userClient.getUserName(1L)).thenReturn("testuser");

        // Act
        List<NotificationResponseDto> result = notificationService.getNotificationsForUser(2L, true);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getDetail().contains("testuser"));
        verify(notificationRepository).findByReceivedIdOrderByIdDesc(2L);
        verify(notificationRepository, never()).findByReceivedIdAndDateSeenAtIsNull(anyLong());
    }

    @Test
    void testGetNotificationsForUser_UnseenOnly() {
        // Arrange
        List<Notification> notifications = new ArrayList<>();
        notifications.add(testNotification);

        when(notificationRepository.findByReceivedIdAndDateSeenAtIsNull(2L)).thenReturn(notifications);
        when(userClient.getUserName(1L)).thenReturn("testuser");

        // Act
        List<NotificationResponseDto> result = notificationService.getNotificationsForUser(2L, false);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(notificationRepository).findByReceivedIdAndDateSeenAtIsNull(2L);
        verify(notificationRepository, never()).findByReceivedIdOrderByIdDesc(anyLong());
    }

    @Test
    void testGetNotificationsForUser_TransactionCreated() {
        // Arrange
        testNotification.setAction(Action.TRANSACTION_CREATED);
        List<Notification> notifications = new ArrayList<>();
        notifications.add(testNotification);

        when(notificationRepository.findByReceivedIdOrderByIdDesc(2L)).thenReturn(notifications);
        when(userClient.getUserName(1L)).thenReturn("testuser");

        // Act
        List<NotificationResponseDto> result = notificationService.getNotificationsForUser(2L, true);

        // Assert
        assertNotNull(result);
        assertTrue(result.get(0).getDetail().contains("Nouvelle transaction créee"));
        assertTrue(result.get(0).getDetail().contains("testuser"));
    }

    @Test
    void testGetNotificationsForUser_TransactionVerified() {
        // Arrange
        testNotification.setAction(Action.TRANSACTION_VERIFIED);
        List<Notification> notifications = new ArrayList<>();
        notifications.add(testNotification);

        when(notificationRepository.findByReceivedIdOrderByIdDesc(2L)).thenReturn(notifications);
        when(userClient.getUserName(1L)).thenReturn("testuser");

        // Act
        List<NotificationResponseDto> result = notificationService.getNotificationsForUser(2L, true);

        // Assert
        assertNotNull(result);
        assertTrue(result.get(0).getDetail().contains("Transaction vérifiée"));
        assertTrue(result.get(0).getDetail().contains("SUCCES"));
    }

    @Test
    void testGetNotificationsForUser_TransactionVerifiedNok() {
        // Arrange
        testNotification.setAction(Action.TRANSACTION_VERIFIED_NOK);
        List<Notification> notifications = new ArrayList<>();
        notifications.add(testNotification);

        when(notificationRepository.findByReceivedIdOrderByIdDesc(2L)).thenReturn(notifications);
        when(userClient.getUserName(1L)).thenReturn("testuser");

        // Act
        List<NotificationResponseDto> result = notificationService.getNotificationsForUser(2L, true);

        // Assert
        assertNotNull(result);
        assertTrue(result.get(0).getDetail().contains("Transaction vérifiée"));
        assertTrue(result.get(0).getDetail().contains("FAILED"));
    }

    @Test
    void testMarkAllAsSeen() {
        // Arrange
        when(notificationRepository.markAllAsSeen(2L)).thenReturn(1);

        // Act
        notificationService.markAllAsSeen(2L);

        // Assert
        verify(notificationRepository).markAllAsSeen(2L);
    }

    @Test
    void testMarkAsSeen_Success() {
        // Arrange
        testNotification.setDateSeenAt(null);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        Notification result = notificationService.markAsSeen(1L);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getDateSeenAt());
        verify(notificationRepository).findById(1L);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testMarkAsSeen_NotFound() {
        // Arrange
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            notificationService.markAsSeen(999L);
        });

        assertEquals("Notification not found", exception.getMessage());
        verify(notificationRepository).findById(999L);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void testTimeAgo_JustNow() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime created = now.minusSeconds(30);
        testNotification.setDateCreatedAt(created);
        List<Notification> notifications = new ArrayList<>();
        notifications.add(testNotification);

        when(notificationRepository.findByReceivedIdOrderByIdDesc(2L)).thenReturn(notifications);
        when(userClient.getUserName(1L)).thenReturn("testuser");

        // Act
        List<NotificationResponseDto> result = notificationService.getNotificationsForUser(2L, true);

        // Assert
        assertNotNull(result);
        assertEquals("à l'instant", result.get(0).getTimePassed());
    }

    @Test
    void testTimeAgo_Minutes() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime created = now.minusMinutes(15);
        testNotification.setDateCreatedAt(created);
        List<Notification> notifications = new ArrayList<>();
        notifications.add(testNotification);

        when(notificationRepository.findByReceivedIdOrderByIdDesc(2L)).thenReturn(notifications);
        when(userClient.getUserName(1L)).thenReturn("testuser");

        // Act
        List<NotificationResponseDto> result = notificationService.getNotificationsForUser(2L, true);

        // Assert
        assertNotNull(result);
        assertTrue(result.get(0).getTimePassed().contains("min"));
    }

    @Test
    void testTimeAgo_Hours() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime created = now.minusHours(3);
        testNotification.setDateCreatedAt(created);
        List<Notification> notifications = new ArrayList<>();
        notifications.add(testNotification);

        when(notificationRepository.findByReceivedIdOrderByIdDesc(2L)).thenReturn(notifications);
        when(userClient.getUserName(1L)).thenReturn("testuser");

        // Act
        List<NotificationResponseDto> result = notificationService.getNotificationsForUser(2L, true);

        // Assert
        assertNotNull(result);
        assertTrue(result.get(0).getTimePassed().contains("heure"));
    }
}
