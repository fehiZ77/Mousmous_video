package com.moustass.transactions_service.service;

import com.moustass.transactions_service.client.minio.MinIOService;
import com.moustass.transactions_service.client.kms.KmsClient;
import com.moustass.transactions_service.client.users.UserClient;
import com.moustass.transactions_service.dto.TransactionResponseDto;
import com.moustass.transactions_service.dto.TransactionRequestDto;
import com.moustass.transactions_service.dto.VerifyTransactionDto;
import com.moustass.transactions_service.entity.Media;
import com.moustass.transactions_service.entity.Transaction;
import com.moustass.transactions_service.entity.TransactionStatus;
import com.moustass.transactions_service.repository.MediaRepository;
import com.moustass.transactions_service.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final MediaRepository mediaRepository;
    private final KmsClient kmsClient;
    private final MinIOService minIOService;
    private final UserClient userClient;

    public TransactionService(TransactionRepository transactionRepository, MediaRepository mediaRepository, KmsClient kmsClient, MinIOService minIOService, UserClient userClient) {
        this.transactionRepository = transactionRepository;
        this.mediaRepository = mediaRepository;
        this.kmsClient = kmsClient;
        this.minIOService = minIOService;
        this.userClient = userClient;
    }

    @Transactional
    public boolean verifyTransaction(VerifyTransactionDto dto, String token) throws Exception{
        try {
            Media media = mediaRepository.findByTransactionId(dto.getTransactionId())
                    .orElseThrow(() -> new RuntimeException("Media not found for transaction " + dto.getTransactionId()));

            boolean isFileOk = kmsClient.verifyFile(
                    dto.getPublicKey(),
                    media.getVideoHash(),
                    media.getVideoSig(),
                    token
            );

            if(isFileOk){
                transactionRepository.updateStatus(
                        dto.getTransactionId(),
                        TransactionStatus.VERIFIED
                );
            }
            return isFileOk;
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public List<TransactionResponseDto> getTransactions(Long userId, String token, boolean isOwner){
        List<Transaction> transactions = isOwner ? transactionRepository.findByOwnerId(userId)
                                        : transactionRepository.findByRecipientId(userId);

        return transactions.stream()
                .map(transaction -> {
                    // Récupérer le média associé
                    Media media = mediaRepository.findByTransactionId(transaction.getId())
                            .orElseThrow(() -> new RuntimeException("Media not found for transaction " + transaction.getId()));

                    // Récupérer le nom d'utilisateur du créateur
                    String userName = userClient.getUserName(isOwner ? transaction.getRecipientId() : transaction.getOwnerId(), token);

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
    public void createTransaction(TransactionRequestDto dto) throws Exception{
        Transaction transaction = new Transaction(dto.getOwner_id(), dto.getRecipient_id(), dto.getAmount(), dto.getValidity());
        String fileHash = hashFile(dto.getVideo());
        String signature = kmsClient.signFile(
                dto.getOwner_id(),
                dto.getKeyId(),
                fileHash,
                dto.getToken()
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
    }

    /**
     * Hash un fichier avec SHA-256
     */
    private String hashFile(MultipartFile file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(file.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }
}
