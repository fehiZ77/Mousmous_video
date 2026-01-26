package com.moustass.transactions_service.service;

import com.moustass.transactions_service.TransactionException.GlobalException;
import com.moustass.transactions_service.client.TransactionAction;
import com.moustass.transactions_service.client.audit.AuditClient;
import com.moustass.transactions_service.client.audit.AuditRequestDto;
import com.moustass.transactions_service.client.minio.MinIOService;
import com.moustass.transactions_service.client.kms.KmsClient;
import com.moustass.transactions_service.client.notification.NotificationClient;
import com.moustass.transactions_service.client.notification.NotificationRequestDto;
import com.moustass.transactions_service.client.users.UserClient;
import com.moustass.transactions_service.dto.TransactionResponseDto;
import com.moustass.transactions_service.dto.TransactionRequestDto;
import com.moustass.transactions_service.dto.VerifyTransactionDto;
import com.moustass.transactions_service.entity.Media;
import com.moustass.transactions_service.entity.Transaction;
import com.moustass.transactions_service.entity.TransactionStatus;
import com.moustass.transactions_service.repository.MediaRepository;
import com.moustass.transactions_service.repository.TransactionRepository;
import io.minio.errors.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final MediaRepository mediaRepository;

    // Service
    private final KmsClient kmsClient;
    private final MinIOService minIOService;
    private final UserClient userClient;
    private final AuditClient auditClient;
    private final NotificationClient notificationClient;

    private final String serviceName = "TRANSACTION";

    public TransactionService(
            TransactionRepository transactionRepository,
            MediaRepository mediaRepository,
            KmsClient kmsClient,
            MinIOService minIOService,
            UserClient userClient,
            AuditClient auditClient,
            NotificationClient notificationClient
    ) {
        this.transactionRepository = transactionRepository;
        this.mediaRepository = mediaRepository;
        this.kmsClient = kmsClient;
        this.minIOService = minIOService;
        this.userClient = userClient;
        this.auditClient = auditClient;
        this.notificationClient = notificationClient;
    }

    @Transactional
    public boolean verifyTransaction(VerifyTransactionDto dto) throws GlobalException {
        try {
            Media media = mediaRepository.findByTransactionId(dto.getTransactionId())
                    .orElseThrow(() -> new RuntimeException("Media not found for transaction " + dto.getTransactionId()));

            boolean isFileOk = kmsClient.verifyFile(
                    dto.getPublicKey(),
                    media.getVideoHash(),
                    media.getVideoSig()
            );

            if(isFileOk){
                transactionRepository.updateStatus(
                        dto.getTransactionId(),
                        TransactionStatus.VERIFIED
                );
            }
            String transactionStatusStr = isFileOk ? TransactionAction.TRANSACTION_VERIFIED.name() : TransactionAction.TRANSACTION_VERIFIED_NOK.name();

            auditClient.createAudit(
                    new AuditRequestDto(
                            serviceName,
                            transactionStatusStr,
                            isFileOk ? "Transaction valide" : "Transaction non valide",
                            isFileOk ? AuditRequestDto.Status.SUCCES : AuditRequestDto.Status.FAILED,
                            LocalDateTime.now().toString()
                    )
            );

            Optional<Transaction> transaction = transactionRepository.findById(dto.getTransactionId());

            notificationClient.createNotification(
                    new NotificationRequestDto(
                            transaction.map(Transaction::getRecipientId).orElse(null),
                            transaction.map(Transaction::getOwnerId).orElse(null),
                            transactionStatusStr
                    )
            );

            return isFileOk;
        } catch (Exception e) {
            throw new GlobalException(e.getMessage());
        }
    }

    public List<TransactionResponseDto> getTransactions(Long userId, boolean isOwner){
        List<Transaction> transactions = isOwner ? transactionRepository.findByOwnerId(userId)
                                        : transactionRepository.findByRecipientId(userId);

        return transactions.stream()
                .map(transaction -> {
                    // Récupérer le média associé
                    Media media = mediaRepository.findByTransactionId(transaction.getId())
                            .orElseThrow(() -> new RuntimeException("Media not found for transaction " + transaction.getId()));

                    // Récupérer le nom d'utilisateur du créateur
                    String userName = userClient.getUserName(isOwner ? transaction.getRecipientId() : transaction.getOwnerId());

                    // Construire le DTO
                    return new TransactionResponseDto(
                            transaction.getId(),
                            transaction.getCreatedAt(),
                            userName,
                            transaction.getAmount(),
                            media.getObjectName(),
                            transaction.getStatus().name(),
                            media.getSignPk()
                    );

                }).toList();
    }

    @Transactional
    public void createTransaction(TransactionRequestDto dto) throws GlobalException, NoSuchAlgorithmException, IOException, ServerException, InsufficientDataException, ErrorResponseException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        Transaction transaction = new Transaction(dto.getOwnerId(), dto.getRecipientId(), dto.getAmount(), dto.getValidity());
        String fileHash = hashFile(dto.getVideo());
        String signature = kmsClient.signFile(
                dto.getOwnerId(),
                dto.getKeyId(),
                fileHash,
                readPrivateKeyFromFile(dto.getSk())
        );
        String storageMiniIO = minIOService.processMinIOStorage(dto.getVideo());

        transactionRepository.save(transaction);
        Media media = new Media(
                transaction,
                storageMiniIO,
                fileHash,
                signature,
                dto.getKeyId().toString(),
                dto.getPublicKey()
        );

        mediaRepository.save(media);

        auditClient.createAudit(
                new AuditRequestDto(
                        serviceName,
                        TransactionAction.TRANSACTION_CREATED.name(),
                        "Transaction de " + dto.getOwnerId() + " vers " + dto.getRecipientId() + ": MUR " +dto.getAmount(),
                        AuditRequestDto.Status.SUCCES,
                        LocalDateTime.now().toString()
                )
        );

        notificationClient.createNotification(
                new NotificationRequestDto(
                        transaction.getOwnerId(),
                        transaction.getRecipientId(),
                        TransactionAction.TRANSACTION_CREATED.name()
                )
        );
    }


    // =========================== PRIVATE FUNCTION ===========================
    /**
     * Hash un fichier avec SHA-256
     */
    private String hashFile(MultipartFile file) throws GlobalException, NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(file.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }

    private String readPrivateKeyFromFile(MultipartFile privateKeyFile) throws GlobalException, IOException {

        if (privateKeyFile == null || privateKeyFile.isEmpty()) {
            throw new IllegalArgumentException("Fichier de clé privée manquant");
        }

        return new String(
                privateKeyFile.getBytes(),
                StandardCharsets.UTF_8
        );
    }
}
