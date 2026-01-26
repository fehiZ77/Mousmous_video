package com.moustass_video.kms_service.controller;

import com.moustass_video.kms_service.KmsException.GlobalException;
import com.moustass_video.kms_service.dto.*;
import com.moustass_video.kms_service.entity.KeyStatus;
import com.moustass_video.kms_service.entity.UserKeys;
import com.moustass_video.kms_service.service.UserKeyManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeyControllerTest {

    @Mock
    private UserKeyManagementService keyManagementService;

    @InjectMocks
    private KeyController keyController;

    private GenerateKeyRequestDto generateKeyRequestDto;
    private GeneratedKeyResultDto generatedKeyResultDto;
    private UserKeys userKey;
    private FileToSignDto fileToSignDto;
    private VerifySignDto verifySignDto;

    @BeforeEach
    void setUp() {
        generateKeyRequestDto = new GenerateKeyRequestDto();
        generateKeyRequestDto.setUserId(1L);
        generateKeyRequestDto.setKeyName("test-key");
        generateKeyRequestDto.setValidty(12);

        userKey = new UserKeys();
        userKey.setId(1L);
        userKey.setUserId(1L);
        userKey.setKeyName("test-key");
        userKey.setStatus(KeyStatus.ACTIVE);
        userKey.setCreatedAt(LocalDateTime.now());

        generatedKeyResultDto = new GeneratedKeyResultDto("private-key-base64", userKey);

        fileToSignDto = new FileToSignDto();
        fileToSignDto.setFileHash("hash-base64");
        fileToSignDto.setPrivateKey("private-key-base64");

        verifySignDto = new VerifySignDto();
        verifySignDto.setFileHash("hash-base64");
        verifySignDto.setSignature("signature-base64");
        verifySignDto.setPublicKey("public-key-base64");
    }

    @Test
    void testGenerateKeyPair_Success() throws Exception {
        // Arrange
        when(keyManagementService.generateKeyPair(any(GenerateKeyRequestDto.class)))
                .thenReturn(generatedKeyResultDto);

        // Act
        ResponseEntity<?> response = keyController.generateKeyPair(generateKeyRequestDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(keyManagementService).generateKeyPair(any(GenerateKeyRequestDto.class));
    }

    @Test
    void testGenerateKeyPair_Failure() throws Exception {
        // Arrange
        when(keyManagementService.generateKeyPair(any(GenerateKeyRequestDto.class)))
                .thenThrow(new IllegalArgumentException("Key name already exists"));

        // Act
        ResponseEntity<?> response = keyController.generateKeyPair(generateKeyRequestDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Key name already exists", response.getBody());
        verify(keyManagementService).generateKeyPair(any(GenerateKeyRequestDto.class));
    }

    @Test
    void testListKeys_Success() {
        // Arrange
        List<UserKeys> keys = new ArrayList<>();
        keys.add(userKey);

        when(keyManagementService.findAllById(1L)).thenReturn(keys);

        // Act
        ResponseEntity<?> response = keyController.listKeys(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(keyManagementService).findAllById(1L);
    }

    @Test
    void testListKeys_Failure() {
        // Arrange
        when(keyManagementService.findAllById(1L)).thenThrow(new RuntimeException("Error"));

        // Act
        ResponseEntity<?> response = keyController.listKeys(1L);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error", response.getBody());
        verify(keyManagementService).findAllById(1L);
    }

    @Test
    void testListValidKeys_Success() {
        // Arrange
        List<UserKeys> keys = new ArrayList<>();
        keys.add(userKey);

        when(keyManagementService.getValidUserKeys(1L)).thenReturn(keys);

        // Act
        ResponseEntity<?> response = keyController.listValidKeys(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(keyManagementService).getValidUserKeys(1L);
    }

    @Test
    void testListValidKeys_Failure() {
        // Arrange
        when(keyManagementService.getValidUserKeys(1L)).thenThrow(new RuntimeException("Error"));

        // Act
        ResponseEntity<?> response = keyController.listValidKeys(1L);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error", response.getBody());
        verify(keyManagementService).getValidUserKeys(1L);
    }

    @Test
    void testRevoke_Success() {
        // Arrange
        RevokeKeyRequestDto dto = new RevokeKeyRequestDto();
        dto.setKeyId(1L);
        dto.setUserId(1L);

        when(keyManagementService.revokeKey(dto)).thenReturn(userKey);

        // Act
        ResponseEntity<?> response = keyController.revoke(dto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(keyManagementService).revokeKey(dto);
    }

    @Test
    void testRevoke_Failure() {
        // Arrange
        RevokeKeyRequestDto dto = new RevokeKeyRequestDto();
        dto.setKeyId(999L);
        dto.setUserId(1L);

        when(keyManagementService.revokeKey(dto)).thenThrow(new RuntimeException("Key not found"));

        // Act
        ResponseEntity<?> response = keyController.revoke(dto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Key not found", response.getBody());
        verify(keyManagementService).revokeKey(dto);
    }

    @Test
    void testSignFile_Success() throws Exception {
        // Arrange
        when(keyManagementService.signFileWithUserKey(fileToSignDto)).thenReturn("signature-base64");

        // Act
        ResponseEntity<String> response = keyController.signFile(fileToSignDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("signature-base64", response.getBody());
        verify(keyManagementService).signFileWithUserKey(fileToSignDto);
    }

    @Test
    void testSignFile_Failure() throws Exception {
        // Arrange
        when(keyManagementService.signFileWithUserKey(fileToSignDto))
                .thenThrow(new GlobalException("Signing failed"));

        // Act
        ResponseEntity<String> response = keyController.signFile(fileToSignDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Signing failed", response.getBody());
        verify(keyManagementService).signFileWithUserKey(fileToSignDto);
    }

    @Test
    void testVerifySignature_Success() throws Exception {
        // Arrange
        when(keyManagementService.verifySignature(verifySignDto)).thenReturn(true);

        // Act
        ResponseEntity<Boolean> response = keyController.verifySignature(verifySignDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
        verify(keyManagementService).verifySignature(verifySignDto);
    }

    @Test
    void testVerifySignature_Failure() throws Exception {
        // Arrange
        when(keyManagementService.verifySignature(verifySignDto))
                .thenThrow(new GlobalException("Verification failed"));

        // Act
        ResponseEntity<Boolean> response = keyController.verifySignature(verifySignDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(false, response.getBody());
        verify(keyManagementService).verifySignature(verifySignDto);
    }
}
