package com.moustass_video.kms_service.service;

import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Service
public class SignatureService {
    /**
     * Signe un hash avec une clé privée RSA
     */
    public String signHash(String hashBase64, String privateKeyBase64) throws Exception {
        // Décoder la clé privée
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        // Signer
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(Base64.getDecoder().decode(hashBase64));

        byte[] signatureBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    /**
     * Vérifie une signature avec une clé publique RSA
     */
    public boolean verifySignature(String hashBase64, String signatureBase64, String publicKeyBase64) throws Exception {

        try {
            // Décoder la clé publique
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            java.security.spec.X509EncodedKeySpec keySpec = new java.security.spec.X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            // Vérifier la signature
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(Base64.getDecoder().decode(hashBase64));

            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
            return signature.verify(signatureBytes);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid Base64 public key");
        }
    }
}
