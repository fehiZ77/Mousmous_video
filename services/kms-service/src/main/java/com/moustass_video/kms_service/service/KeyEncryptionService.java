package com.moustass_video.kms_service.service;

import com.moustass_video.kms_service.dto.EncryptedSkDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class KeyEncryptionService {
    @Value("${master.encryption.key}")
    private String masterKeyBase64;

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;

    /**
     * Chiffre une clé privée avec la master key
     */
    public EncryptedSkDto encryptPrivateKey(String privateKey) throws Exception {
        SecretKeySpec masterKey = new SecretKeySpec(
                Base64.getDecoder().decode(masterKeyBase64),
                "AES"
        );

        Cipher cipher = Cipher.getInstance(ALGORITHM);

        // Générer un IV aléatoire
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, masterKey, spec);

        byte[] encrypted = cipher.doFinal(
                privateKey.getBytes(StandardCharsets.UTF_8)
        );

        return new EncryptedSkDto(
                Base64.getEncoder().encodeToString(encrypted),
                Base64.getEncoder().encodeToString(iv)
        );
    }

    /**
     * Déchiffre une clé privée avec la master key
     */
    public String decryptPrivateKey(String encryptedKey, String ivBase64) throws Exception {
        SecretKeySpec masterKey = new SecretKeySpec(
                Base64.getDecoder().decode(masterKeyBase64),
                "AES"
        );

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(
                GCM_TAG_LENGTH,
                Base64.getDecoder().decode(ivBase64)
        );
        cipher.init(Cipher.DECRYPT_MODE, masterKey, spec);

        byte[] decrypted = cipher.doFinal(
                Base64.getDecoder().decode(encryptedKey)
        );

        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
