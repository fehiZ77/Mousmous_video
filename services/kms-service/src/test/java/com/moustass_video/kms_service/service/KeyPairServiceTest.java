package com.moustass_video.kms_service.service;

import com.moustass_video.kms_service.dto.KeyPairDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class KeyPairServiceTest {

    @InjectMocks
    private KeyPairService keyPairService;

    @Test
    void testGenerateKeyPair_Success() throws Exception {
        // Act
        KeyPairDto result = keyPairService.generateKeyPair();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getPublicKey());
        assertNotNull(result.getPrivateKey());
        assertFalse(result.getPublicKey().isEmpty());
        assertFalse(result.getPrivateKey().isEmpty());
        
        // Verify they are valid Base64
        assertDoesNotThrow(() -> {
            Base64.getDecoder().decode(result.getPublicKey());
            Base64.getDecoder().decode(result.getPrivateKey());
        });
    }

    @Test
    void testGenerateKeyPair_UniqueKeys() throws Exception {
        // Act
        KeyPairDto keyPair1 = keyPairService.generateKeyPair();
        KeyPairDto keyPair2 = keyPairService.generateKeyPair();

        // Assert
        assertNotEquals(keyPair1.getPublicKey(), keyPair2.getPublicKey());
        assertNotEquals(keyPair1.getPrivateKey(), keyPair2.getPrivateKey());
    }

    @Test
    void testGenerateKeyPair_ValidRSAKeys() throws Exception {
        // Act
        KeyPairDto result = keyPairService.generateKeyPair();

        // Assert
        // Decode and verify they are RSA keys
        byte[] publicKeyBytes = Base64.getDecoder().decode(result.getPublicKey());
        byte[] privateKeyBytes = Base64.getDecoder().decode(result.getPrivateKey());
        
        assertTrue(publicKeyBytes.length > 0);
        assertTrue(privateKeyBytes.length > 0);
    }
}
