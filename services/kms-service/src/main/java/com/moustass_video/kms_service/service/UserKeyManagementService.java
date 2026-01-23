package com.moustass_video.kms_service.service;

import com.moustass_video.kms_service.client.audit.AuditAction;
import com.moustass_video.kms_service.client.audit.AuditClient;
import com.moustass_video.kms_service.client.audit.AuditRequestDto;
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
    private final SignatureService signatureService;
    private final UserKeyRepository userKeyRepository;
    private final AuditClient auditClient;

    private final String serviceName = "AUTH";

    public UserKeyManagementService(
            KeyPairService keyPairService,
            SignatureService signatureService,
            UserKeyRepository repository,
            AuditClient auditClient
    ) {
        this.keyPairService = keyPairService;
        this.signatureService = signatureService;
        this.userKeyRepository = repository;
        this.auditClient = auditClient;
    }

    public UserKeys revokeKey(RevokeKeyRequestDto dto) {
        UserKeys key = userKeyRepository.findByIdAndUserId(dto.getKeyId(), dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Key not found"));

        key.setStatus(KeyStatus.REVOKED);
        key = userKeyRepository.save(key);
        auditClient.createAudit(
                new AuditRequestDto(
                        serviceName,
                        AuditAction.REVOKE_KEY.name(),
                        "Revoke key : " + key.getId(),
                        AuditRequestDto.Status.SUCCES,
                        LocalDateTime.now().toString()
                )
        );

        return key;
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
    public GeneratedKeyResultDto generateKeyPair(GenerateKeyRequestDto dto) throws Exception {
        // Vérifier unicité du nom
        if (userKeyRepository.existsByUserIdAndKeyName(dto.getUserId(), dto.getKeyName())) {
            throw new IllegalArgumentException("Le nom de clé existe déjà");
        }

        // Générer la paire
        KeyPairDto keyPair = keyPairService.generateKeyPair();

        // Sauvegarder
        UserKeys entity = new UserKeys();
        entity.setUserId(dto.getUserId());
        entity.setKeyName(dto.getKeyName());
        entity.setPublicKey(keyPair.getPublicKey());
        entity.setStatus(KeyStatus.ACTIVE);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setExpiredAt(now.plusMonths(dto.getValidity()));

        entity = userKeyRepository.save(entity);
        auditClient.createAudit(
                new AuditRequestDto(
                        serviceName,
                        AuditAction.CREATE_KEY.name(),
                        "Create key : " + entity.getId(),
                        AuditRequestDto.Status.SUCCES,
                        LocalDateTime.now().toString()
                )
        );
        return new GeneratedKeyResultDto(keyPair.getPrivateKey(), entity);
    }

    /**
     * Signe un fichier avec la clé privée de l'utilisateur
     */
    public String signFileWithUserKey(FileToSignDto dto)
            throws Exception {
        // Signer le hash
        return signatureService.signHash(dto.getFileHash(), dto.getPrivateKey());
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
