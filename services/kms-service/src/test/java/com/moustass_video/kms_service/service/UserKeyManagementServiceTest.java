package com.moustass_video.kms_service.service;

import com.moustass_video.kms_service.client.audit.AuditAction;
import com.moustass_video.kms_service.client.audit.AuditClient;
import com.moustass_video.kms_service.client.audit.AuditRequestDto;
import com.moustass_video.kms_service.dto.*;
import com.moustass_video.kms_service.entity.KeyStatus;
import com.moustass_video.kms_service.entity.UserKeys;
import com.moustass_video.kms_service.repository.UserKeyRepository;
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
class UserKeyManagementServiceTest {

    @Mock
    private KeyPairService keyPairService;

    @Mock
    private SignatureService signatureService;

    @Mock
    private UserKeyRepository userKeyRepository;

    @Mock
    private AuditClient auditClient;

    @InjectMocks
    private UserKeyManagementService userKeyManagementService;

    private UserKeys testUserKey;
    private GenerateKeyRequestDto generateKeyRequestDto;
    private KeyPairDto keyPairDto;

    @BeforeEach
    void setUp() {
        testUserKey = new UserKeys();
        testUserKey.setId(1L);
        testUserKey.setUserId(1L);
        testUserKey.setKeyName("test-key");
        testUserKey.setPublicKey("public-key-base64");
        testUserKey.setStatus(KeyStatus.ACTIVE);
        testUserKey.setCreatedAt(LocalDateTime.now());
        testUserKey.setExpiredAt(LocalDateTime.now().plusMonths(12));

        generateKeyRequestDto = new GenerateKeyRequestDto();
        generateKeyRequestDto.setUserId(1L);
        generateKeyRequestDto.setKeyName("new-key");
        generateKeyRequestDto.setValidty(12);

        keyPairDto = new KeyPairDto("public-key", "private-key");
    }

    @Test
    void testRevokeKey_Success() {
        // Arrange
        RevokeKeyRequestDto dto = new RevokeKeyRequestDto();
        dto.setKeyId(1L);
        dto.setUserId(1L);

        when(userKeyRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testUserKey));
        when(userKeyRepository.save(any(UserKeys.class))).thenReturn(testUserKey);

        // Act
        UserKeys result = userKeyManagementService.revokeKey(dto);

        // Assert
        assertNotNull(result);
        assertEquals(KeyStatus.REVOKED, result.getStatus());
        verify(userKeyRepository).findByIdAndUserId(1L, 1L);
        verify(userKeyRepository).save(any(UserKeys.class));
        verify(auditClient).createAudit(any(AuditRequestDto.class));
    }

    @Test
    void testRevokeKey_KeyNotFound() {
        // Arrange
        RevokeKeyRequestDto dto = new RevokeKeyRequestDto();
        dto.setKeyId(999L);
        dto.setUserId(1L);

        when(userKeyRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userKeyManagementService.revokeKey(dto);
        });

        assertEquals("Key not found", exception.getMessage());
        verify(userKeyRepository).findByIdAndUserId(999L, 1L);
        verify(userKeyRepository, never()).save(any(UserKeys.class));
    }

    @Test
    void testGetValidUserKeys_Success() {
        // Arrange
        List<UserKeys> keys = new ArrayList<>();
        keys.add(testUserKey);

        when(userKeyRepository.findValidKeysByUserId(eq(1L), eq(KeyStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(keys);

        // Act
        List<UserKeys> result = userKeyManagementService.getValidUserKeys(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userKeyRepository).findValidKeysByUserId(eq(1L), eq(KeyStatus.ACTIVE), any(LocalDateTime.class));
    }

    @Test
    void testUpdateExpiredKeys() {
        // Arrange
        doNothing().when(userKeyRepository).expireOldKeys(eq(KeyStatus.EXPIRED), any(LocalDateTime.class));

        // Act
        userKeyManagementService.updateExpiredKeys();

        // Assert
        verify(userKeyRepository).expireOldKeys(eq(KeyStatus.EXPIRED), any(LocalDateTime.class));
    }

    @Test
    void testFindAllById_Success() {
        // Arrange
        List<UserKeys> keys = new ArrayList<>();
        keys.add(testUserKey);

        when(userKeyRepository.findByUserId(1L)).thenReturn(keys);

        // Act
        List<UserKeys> result = userKeyManagementService.findAllById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userKeyRepository).findByUserId(1L);
    }

    @Test
    void testGenerateKeyPair_Success() throws Exception {
        // Arrange
        when(userKeyRepository.existsByUserIdAndKeyName(1L, "new-key")).thenReturn(false);
        when(keyPairService.generateKeyPair()).thenReturn(keyPairDto);
        when(userKeyRepository.save(any(UserKeys.class))).thenReturn(testUserKey);

        // Act
        GeneratedKeyResultDto result = userKeyManagementService.generateKeyPair(generateKeyRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals("private-key", result.getPrivateKey());
        assertNotNull(result.getSavedKey());
        verify(userKeyRepository).existsByUserIdAndKeyName(1L, "new-key");
        verify(keyPairService).generateKeyPair();
        verify(userKeyRepository).save(any(UserKeys.class));
        verify(auditClient).createAudit(any(AuditRequestDto.class));
    }

    @Test
    void testGenerateKeyPair_KeyNameExists() throws Exception {
        // Arrange
        when(userKeyRepository.existsByUserIdAndKeyName(1L, "new-key")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userKeyManagementService.generateKeyPair(generateKeyRequestDto);
        });

        assertEquals("Le nom de clé existe déjà", exception.getMessage());
        verify(userKeyRepository).existsByUserIdAndKeyName(1L, "new-key");
        verify(keyPairService, never()).generateKeyPair();
    }

    @Test
    void testSignFileWithUserKey_Success() throws Exception {
        // Arrange
        FileToSignDto dto = new FileToSignDto();
        dto.setFileHash("file-hash-base64");
        dto.setPrivateKey("private-key-base64");

        when(signatureService.signHash("file-hash-base64", "private-key-base64"))
                .thenReturn("signature-base64");

        // Act
        String result = userKeyManagementService.signFileWithUserKey(dto);

        // Assert
        assertEquals("signature-base64", result);
        verify(signatureService).signHash("file-hash-base64", "private-key-base64");
    }

    @Test
    void testSignFileWithUserKey_Failure() throws Exception {
        // Arrange
        FileToSignDto dto = new FileToSignDto();
        dto.setFileHash("file-hash-base64");
        dto.setPrivateKey("invalid-key");

        when(signatureService.signHash(anyString(), anyString()))
                .thenThrow(new RuntimeException("Signing failed"));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            userKeyManagementService.signFileWithUserKey(dto);
        });

        assertNotNull(exception);
        verify(signatureService).signHash(anyString(), anyString());
    }

    @Test
    void testVerifySignature_Success() throws Exception {
        // Arrange
        VerifySignDto dto = new VerifySignDto();
        dto.setFileHash("file-hash-base64");
        dto.setSignature("signature-base64");
        dto.setPublicKey("public-key-base64");

        when(signatureService.verifySignature("file-hash-base64", "signature-base64", "public-key-base64"))
                .thenReturn(true);

        // Act
        boolean result = userKeyManagementService.verifySignature(dto);

        // Assert
        assertTrue(result);
        verify(signatureService).verifySignature("file-hash-base64", "signature-base64", "public-key-base64");
    }

    @Test
    void testVerifySignature_Failure() throws Exception {
        // Arrange
        VerifySignDto dto = new VerifySignDto();
        dto.setFileHash("file-hash-base64");
        dto.setSignature("invalid-signature");
        dto.setPublicKey("public-key-base64");

        when(signatureService.verifySignature(anyString(), anyString(), anyString()))
                .thenReturn(false);

        // Act
        boolean result = userKeyManagementService.verifySignature(dto);

        // Assert
        assertFalse(result);
        verify(signatureService).verifySignature(anyString(), anyString(), anyString());
    }
}
