package com.moustass.audit_service.service;

import com.moustass.audit_service.entity.AuditEvent;
import com.moustass.audit_service.filter.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuditService auditService;

    private AuditEvent auditEvent;
    private Map<String, Object> tokenData;
    private Path testLogDir;

    @BeforeEach
    void setUp() throws Exception {
        // Créer un répertoire de test temporaire
        testLogDir = Files.createTempDirectory("test-logs");
        ReflectionTestUtils.setField(auditService, "LOG_DIRECTORY", testLogDir.toString());
        ReflectionTestUtils.setField(auditService, "LOG_FILE", testLogDir.resolve("audit.log").toString());
        ReflectionTestUtils.setField(auditService, "LOG_DIR", testLogDir);

        auditEvent = new AuditEvent();
        auditEvent.setService("TEST_SERVICE");
        auditEvent.setActionName("TEST_ACTION");
        auditEvent.setActionDetails("Test details");
        auditEvent.setStatus("SUCCESS");

        tokenData = new HashMap<>();
        tokenData.put("userId", 1L);
        tokenData.put("userName", "testuser");

        // Mock SecurityUtil.getCurrentToken() via reflection
        lenient().when(jwtUtils.tokenData(anyString())).thenReturn(tokenData);
    }

    @Test
    void testListLogFiles_Success() throws Exception {
        // Arrange
        Path logFile1 = testLogDir.resolve("audit.log");
        Path logFile2 = testLogDir.resolve("audit-corrupted-2024-01-01.log");
        Files.createFile(logFile1);
        Files.createFile(logFile2);
        Files.createFile(testLogDir.resolve("not-a-log.txt")); // Should be filtered out

        // Act
        List<Path> result = auditService.listLogFiles();

        // Assert
        assertNotNull(result);
        assertTrue(result.size() >= 2);
        assertTrue(result.stream().anyMatch(p -> p.getFileName().toString().equals("audit.log")));
        assertTrue(result.stream().anyMatch(p -> p.getFileName().toString().equals("audit-corrupted-2024-01-01.log")));
    }

    @Test
    void testListLogFiles_EmptyDirectory() {
        // Act
        List<Path> result = auditService.listLogFiles();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCreate_Success() throws Exception {
        // Arrange
        Path logFile = testLogDir.resolve("audit.log");
        Files.createFile(logFile);

        // Act
        auditService.create(auditEvent);

        // Assert
        assertTrue(Files.exists(logFile));
        String content = Files.readString(logFile);
        assertTrue(content.contains("TEST_SERVICE"));
        assertTrue(content.contains("TEST_ACTION"));
        assertTrue(content.contains("testuser"));
        assertTrue(content.contains("hash="));
        assertTrue(content.contains("prev="));
    }

    @Test
    void testCreate_FirstEntry() throws Exception {
        // Arrange - No existing log file

        // Act
        auditService.create(auditEvent);

        // Assert
        Path logFile = testLogDir.resolve("audit.log");
        assertTrue(Files.exists(logFile));
        String content = Files.readString(logFile);
        assertTrue(content.contains("prev=0000")); // Genesis hash
    }

    @Test
    void testVerifyFile_Success() throws Exception {
        // Arrange
        Path logFile = testLogDir.resolve("test.log");
        Files.createFile(logFile);
        
        // Create a valid log entry
        auditService.create(auditEvent);
        String content = Files.readString(testLogDir.resolve("audit.log"));
        Files.writeString(logFile, content);

        // Act
        int result = auditService.verifyFile("test.log");

        // Assert
        assertEquals(0, result); // File is OK
    }

    @Test
    void testVerifyFile_FileNotFound() {
        // Act
        int result = auditService.verifyFile("nonexistent.log");

        // Assert
        assertEquals(-1, result); // Error
    }

    @Test
    void testVerifyFile_Corrupted() throws Exception {
        // Arrange
        Path logFile = testLogDir.resolve("test.log");
        Files.writeString(logFile, "1| data | prev=0000 | hash=WRONG_HASH\n");

        // Act
        int result = auditService.verifyFile("test.log");

        // Assert
        assertTrue(result > 0); // Corruption detected at line result
    }

    @Test
    void testDownloadFile_Success() throws Exception {
        // Arrange
        Path logFile = testLogDir.resolve("test.log");
        Files.writeString(logFile, "Test content");

        // Act
        Resource resource = auditService.downloadFile("test.log");

        // Assert
        assertNotNull(resource);
        assertTrue(resource.exists());
    }

    @Test
    void testDownloadFile_NotFound() {
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            auditService.downloadFile("nonexistent.log");
        });

        assertTrue(exception.getMessage().contains("No file") || exception.getMessage().contains("File error"));
    }

    @Test
    void testRotateIfCorrupted() throws Exception {
        // Arrange
        Path logFile = testLogDir.resolve("audit.log");
        Files.writeString(logFile, "Test content");

        // Act
        auditService.rotateIfCorrupted();

        // Assert
        // Should create a corrupted file with timestamp
        List<Path> files = Files.list(testLogDir)
                .filter(p -> p.getFileName().toString().startsWith("audit-corrupted-"))
                .toList();
        assertFalse(files.isEmpty());
        
        // New audit.log should exist
        assertTrue(Files.exists(testLogDir.resolve("audit.log")));
    }
}
