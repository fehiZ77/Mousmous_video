package com.moustass_video.kms_service.service;

import com.moustass_video.kms_service.dto.KeyPairDto;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class KeyPairService {
    /**
     * Génère une paire de clés RSA
     */
    public KeyPairDto generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048, new SecureRandom());
        KeyPair keyPair = keyGen.generateKeyPair();

        String publicKey = Base64.getEncoder().encodeToString(
                keyPair.getPublic().getEncoded()
        );
        String privateKey = Base64.getEncoder().encodeToString(
                keyPair.getPrivate().getEncoded()
        );

        return new KeyPairDto(publicKey, privateKey);
    }
}
