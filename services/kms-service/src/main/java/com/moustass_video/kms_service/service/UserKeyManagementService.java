package com.moustass_video.kms_service.service;

import com.moustass_video.kms_service.dto.*;
import com.moustass_video.kms_service.entity.KeyStatus;
import com.moustass_video.kms_service.entity.UserKeys;
import com.moustass_video.kms_service.repository.UserKeyRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserKeyManagementService {
    private final KeyPairService keyPairService;
    private final KeyEncryptionService encryptionService;
    private final SignatureService signatureService;
    private final UserKeyRepository userKeyRepository;

    public UserKeyManagementService(KeyPairService keyPairService, KeyEncryptionService encryptionService, SignatureService signatureService, UserKeyRepository repository) {
        this.keyPairService = keyPairService;
        this.encryptionService = encryptionService;
        this.signatureService = signatureService;
        this.userKeyRepository = repository;
    }

    public UserKeys revokeKey(RevokeKeyRequestDto dto) {
        UserKeys key = userKeyRepository.findByIdAndUserId(dto.getKeyId(), dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Key not found"));

        key.setStatus(KeyStatus.REVOKED);
        return userKeyRepository.save(key);
    }

    public List<UserKeys> getValidUserKeys(Long userId) {
        return userKeyRepository.findValidKeysByUserId(
                userId,
                KeyStatus.ACTIVE,
                LocalDateTime.now()
        );
    }

    @Transactional
    public void updateExpiredKeys() {
        userKeyRepository.expireOldKeys(
                KeyStatus.EXPIRED,
                LocalDateTime.now()
        );
    }

    @Scheduled(fixedRate = 3600000) // chaque 60 min
    public void autoExpireKeys() {
        updateExpiredKeys();
    }

    public List<UserKeys> findAllById(Long userId){
        return userKeyRepository.findByUserId(userId);
    }

    @Transactional
    public UserKeys generateKeyPair(GenerateKeyDto dto) throws Exception {
        // Vérifier unicité du nom
        if (userKeyRepository.existsByUserIdAndKeyName(dto.getUserId(), dto.getKeyName())) {
            throw new IllegalArgumentException("Le nom de clé existe déjà");
        }

        // Générer la paire
        KeyPairDto keyPair = keyPairService.generateKeyPair();

        // Chiffrer la clé privée
        EncryptedSkDto encrypted = encryptionService.encryptPrivateKey(
                keyPair.getPrivateKey()
        );

        // Sauvegarder
        UserKeys entity = new UserKeys();
        entity.setUserId(dto.getUserId());
        entity.setKeyName(dto.getKeyName());
        entity.setPublicKey(keyPair.getPublicKey());
        entity.setEncryptedPrivateKey(encrypted.getEncryptedData());
        entity.setIv(encrypted.getIv());
        entity.setStatus(KeyStatus.ACTIVE);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setExpiredAt(now.plusMonths(dto.getValidity()));

        return userKeyRepository.save(entity);
    }

    /**
     * Signe un fichier avec la clé privée de l'utilisateur
     */
    public String signFileWithUserKey(FileToSignDto dto)
            throws Exception {

        // Récupérer la clé de l'utilisateur
        UserKeys userKey = userKeyRepository.findByIdAndUserId(dto.getKeyId(), dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Clé non trouvée ou non autorisée"
                ));

        // Déchiffrer la clé privée
        String privateKey = encryptionService.decryptPrivateKey(
                userKey.getEncryptedPrivateKey(),
                userKey.getIv()
        );

        // Signer le hash
        return signatureService.signHash(dto.getFileHash(), privateKey);
    }

    /**
     * Vérifie une signature avec la clé publique d'un utilisateur
     */
    public boolean verifySignature(VerifySignDto dto)
            throws Exception {

        // Vérifier avec la clé publique
        return signatureService.verifySignature(
                dto.getFileHash(),
                dto.getSignature(),
                dto.getPublicKey()
        );
    }


}
