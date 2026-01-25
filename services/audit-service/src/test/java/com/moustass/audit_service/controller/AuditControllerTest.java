package com.moustass.audit_service.controller;

import com.moustass.audit_service.entity.AuditEvent;
import com.moustass.audit_service.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditControllerTest {

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuditController auditController;

    private AuditEvent auditEvent;

    @BeforeEach
    void setUp() {
        auditEvent = new AuditEvent();
        auditEvent.setService("TEST_SERVICE");
        auditEvent.setActionName("TEST_ACTION");
        auditEvent.setActionDetails("Test details");
        auditEvent.setStatus("SUCCESS");
        
        ReflectionTestUtils.setField(auditController, "LOG_FILE", "/logs/audit.log");
    }

    @Test
    void testCreate_Success() {
        // Arrange
        doNothing().when(auditService).create(any(AuditEvent.class));

        // Act
        ResponseEntity<?> response = auditController.create(auditEvent);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Audit saved", response.getBody());
        verify(auditService).create(any(AuditEvent.class));
    }

    @Test
    void testCreate_Failure() {
        // Arrange
        doThrow(new RuntimeException("Error")).when(auditService).create(any(AuditEvent.class));

        // Act
        ResponseEntity<?> response = auditController.create(auditEvent);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error", response.getBody());
        verify(auditService).create(any(AuditEvent.class));
    }

    @Test
    void testListLogs_Success() {
        // Arrange
        List<Path> paths = new ArrayList<>();
        paths.add(Path.of("/logs/audit.log"));
        paths.add(Path.of("/logs/audit-corrupted-2024-01-01.log"));

        when(auditService.listLogFiles()).thenReturn(paths);

        // Act
        ResponseEntity<?> response = auditController.listLogs();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(auditService).listLogFiles();
    }

    @Test
    void testListLogs_Failure() {
        // Arrange
        when(auditService.listLogFiles()).thenThrow(new RuntimeException("Error"));

        // Act
        ResponseEntity<?> response = auditController.listLogs();

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error", response.getBody());
        verify(auditService).listLogFiles();
    }

    @Test
    void testDownload_Success() throws Exception {
        // Arrange
        Resource resource = mock(Resource.class);
        when(resource.contentLength()).thenReturn(100L);
        when(auditService.downloadFile("test.log")).thenReturn(resource);

        // Act
        ResponseEntity<?> response = auditController.download("test.log");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(auditService).downloadFile("test.log");
    }

    @Test
    void testDownload_Failure() throws Exception {
        // Arrange
        when(auditService.downloadFile("nonexistent.log")).thenThrow(new Exception("File not found"));

        // Act
        ResponseEntity<?> response = auditController.download("nonexistent.log");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("File not found", response.getBody());
        verify(auditService).downloadFile("nonexistent.log");
    }

    @Test
    void testVerify_Success() {
        // Arrange
        when(auditService.verifyFile("test.log")).thenReturn(0);

        // Act
        ResponseEntity<?> response = auditController.verify("test.log");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody());
        verify(auditService).verifyFile("test.log");
    }

    @Test
    void testVerify_Failure() {
        // Arrange
        when(auditService.verifyFile("test.log")).thenThrow(new RuntimeException("Error"));

        // Act
        ResponseEntity<?> response = auditController.verify("test.log");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error", response.getBody());
        verify(auditService).verifyFile("test.log");
    }
}
