package com.moustass.notification_service.controller;

import com.moustass.notification_service.dto.NotificationRequestDto;
import com.moustass.notification_service.dto.NotificationResponseDto;
import com.moustass.notification_service.entity.Action;
import com.moustass.notification_service.entity.Notification;
import com.moustass.notification_service.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private NotificationRequestDto notificationRequestDto;
    private NotificationResponseDto notificationResponseDto;

    @BeforeEach
    void setUp() {
        notificationRequestDto = new NotificationRequestDto();
        notificationRequestDto.setOwnerId(1L);
        notificationRequestDto.setRecipeintId(2L);
        notificationRequestDto.setAction(Action.TRANSACTION_CREATED.name());

        notificationResponseDto = new NotificationResponseDto("Test description", "il y a 5 min");
    }

    @Test
    void testCreateNotification_Success() {
        // Arrange
        Notification notification = new Notification();
        notification.setId(1L);
        when(notificationService.createNotification(any(NotificationRequestDto.class))).thenReturn(notification);

        // Act
        ResponseEntity<?> response = notificationController.create(notificationRequestDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(notificationService).createNotification(any(NotificationRequestDto.class));
    }

    @Test
    void testCreateNotification_Failure() {
        // Arrange
        when(notificationService.createNotification(any(NotificationRequestDto.class)))
                .thenThrow(new RuntimeException("Error"));

        // Act
        ResponseEntity<?> response = notificationController.create(notificationRequestDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error", response.getBody());
        verify(notificationService).createNotification(any(NotificationRequestDto.class));
    }

    @Test
    void testGetNotifications_Success() {
        // Arrange
        List<NotificationResponseDto> notifications = new ArrayList<>();
        notifications.add(notificationResponseDto);

        when(notificationService.getNotificationsForUser(2L, false)).thenReturn(notifications);

        // Act
        ResponseEntity<?> response = notificationController.getNotificationsForUser(2L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(notificationService).getNotificationsForUser(2L, false);
    }

    @Test
    void testGetNotifications_Failure() {
        // Arrange
        when(notificationService.getNotificationsForUser(2L, false))
                .thenThrow(new RuntimeException("Error"));

        // Act
        ResponseEntity<?> response = notificationController.getNotificationsForUser(2L);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error", response.getBody());
        verify(notificationService).getNotificationsForUser(2L, false);
    }

    @Test
    void testMarkAllAsSeen_Success() {
        // Arrange
        doNothing().when(notificationService).markAllAsSeen(2L);

        // Act
        ResponseEntity<?> response = notificationController.markAllAsSeen(2L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(notificationService).markAllAsSeen(2L);
    }

    @Test
    void testMarkAllAsSeen_Failure() {
        // Arrange
        doThrow(new RuntimeException("Error")).when(notificationService).markAllAsSeen(2L);

        // Act
        ResponseEntity<?> response = notificationController.markAllAsSeen(2L);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error", response.getBody());
        verify(notificationService).markAllAsSeen(2L);
    }

}
