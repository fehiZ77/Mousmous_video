package com.moustass_video.kms_service.service;

import com.moustass_video.kms_service.KmsException.GlobalException;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Service
public class SignatureService {
    /**
     * Signe un hash avec une clé privée RSA
     */

    private static final String RSA = "RSA";
    private static final String SHA256_WITH_RSA = "SHA256withRSA";

    public String signHash(String hashBase64, String privateKeyBase64) throws GlobalException {
        try {
            // Décoder la clé privée
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            // Signer
            Signature signature = Signature.getInstance(SHA256_WITH_RSA);
            signature.initSign(privateKey);
            signature.update(Base64.getDecoder().decode(hashBase64));

            byte[] signatureBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (IllegalArgumentException e) {
            throw new GlobalException("Private key is not valid Base64");
        } catch (InvalidKeySpecException e) {
            throw new GlobalException("Private key format is invalid or corrupted");
        } catch (Exception e) {
            throw new GlobalException("Signing failed");
        }
    }

    /**
     * Vérifie une signature avec une clé publique RSA
     */
    public boolean verifySignature(String hashBase64, String signatureBase64, String publicKeyBase64) throws GlobalException {

        // Décoder la clé publique, si elle est invalide -> exception explicite
        try {
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            java.security.spec.X509EncodedKeySpec keySpec = new java.security.spec.X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            Signature signature = Signature.getInstance(SHA256_WITH_RSA);
            signature.initVerify(publicKey);
            signature.update(Base64.getDecoder().decode(hashBase64));

            // Décoder la signature; si base64 invalide -> considérer la vérification comme fausse
            byte[] signatureBytes;
            try {
                signatureBytes = Base64.getDecoder().decode(signatureBase64);
            } catch (IllegalArgumentException e) {
                return false;
            }

            return signature.verify(signatureBytes);
        } catch (IllegalArgumentException e) {
            throw new GlobalException("Invalid Base64 public key");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | SignatureException | InvalidKeyException e) {
            throw new GlobalException(e.getMessage());
        }
    }
}
