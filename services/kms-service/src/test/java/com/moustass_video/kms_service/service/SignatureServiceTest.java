package com.moustass_video.kms_service.service;

import com.moustass_video.kms_service.KmsException.GlobalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Signature;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SignatureServiceTest {

    @InjectMocks
    private SignatureService signatureService;

    private KeyPair keyPair;
    private String testHash;

    @BeforeEach
    void setUp() throws Exception {
        // Generate RSA key pair for testing
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        keyPair = keyGen.generateKeyPair();

        // Create a test hash
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest("test data".getBytes());
        testHash = Base64.getEncoder().encodeToString(hash);
    }

    @Test
    void testSignHash_Success() throws Exception {
        // Arrange
        String privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

        // Act
        String signature = signatureService.signHash(testHash, privateKeyBase64);

        // Assert
        assertNotNull(signature);
        assertFalse(signature.isEmpty());
    }

    @Test
    void testSignHash_InvalidBase64() {
        // Arrange
        String invalidKey = "not-valid-base64!!!";

        // Act & Assert
        GlobalException exception = assertThrows(GlobalException.class, () -> {
            signatureService.signHash(testHash, invalidKey);
        });

        assertEquals("Private key is not valid Base64", exception.getMessage());
    }

    @Test
    void testSignHash_InvalidKeyFormat() {
        // Arrange
        String invalidKey = Base64.getEncoder().encodeToString("not-a-key".getBytes());

        // Act & Assert
        GlobalException exception = assertThrows(GlobalException.class, () -> {
            signatureService.signHash(testHash, invalidKey);
        });

        assertTrue(exception.getMessage().contains("Private key format is invalid") || 
                   exception.getMessage().contains("Signing failed"));
    }

    @Test
    void testVerifySignature_Success() throws Exception {
        // Arrange
        String privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        
        // Sign the hash first
        String signature = signatureService.signHash(testHash, privateKeyBase64);

        // Act
        boolean isValid = signatureService.verifySignature(testHash, signature, publicKeyBase64);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testVerifySignature_InvalidSignature() throws Exception {
        // Arrange
        String publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String invalidSignature = "invalid-signature-base64";

        // Act
        boolean isValid = signatureService.verifySignature(testHash, invalidSignature, publicKeyBase64);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testVerifySignature_InvalidPublicKey() {
        // Arrange
        String invalidKey = "not-valid-base64!!!";

        // Act & Assert
        GlobalException exception = assertThrows(GlobalException.class, () -> {
            signatureService.verifySignature(testHash, "signature", invalidKey);
        });

        assertEquals("Invalid Base64 public key", exception.getMessage());
    }

    @Test
    void testSignAndVerify_CompleteFlow() throws Exception {
        // Arrange
        String privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

        // Act - Sign
        String signature = signatureService.signHash(testHash, privateKeyBase64);
        
        // Act - Verify
        boolean isValid = signatureService.verifySignature(testHash, signature, publicKeyBase64);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testVerifySignature_TamperedHash() throws Exception {
        // Arrange
        String privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        
        // Sign original hash
        String signature = signatureService.signHash(testHash, privateKeyBase64);
        
        // Create tampered hash
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] tamperedHash = digest.digest("tampered data".getBytes());
        String tamperedHashBase64 = Base64.getEncoder().encodeToString(tamperedHash);

        // Act
        boolean isValid = signatureService.verifySignature(tamperedHashBase64, signature, publicKeyBase64);

        // Assert
        assertFalse(isValid);
    }
}
