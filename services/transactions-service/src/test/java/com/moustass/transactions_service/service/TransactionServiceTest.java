package com.moustass.transactions_service.service;

import com.moustass.transactions_service.TransactionException.GlobalException;
import com.moustass.transactions_service.client.TransactionAction;
import com.moustass.transactions_service.client.audit.AuditClient;
import com.moustass.transactions_service.client.audit.AuditRequestDto;
import com.moustass.transactions_service.client.kms.KmsClient;
import com.moustass.transactions_service.client.minio.MinIOService;
import com.moustass.transactions_service.client.notification.NotificationClient;
import com.moustass.transactions_service.client.notification.NotificationRequestDto;
import com.moustass.transactions_service.client.users.UserClient;
import com.moustass.transactions_service.dto.TransactionRequestDto;
import com.moustass.transactions_service.dto.TransactionResponseDto;
import com.moustass.transactions_service.dto.VerifyTransactionDto;
import com.moustass.transactions_service.entity.Media;
import com.moustass.transactions_service.entity.Transaction;
import com.moustass.transactions_service.entity.TransactionStatus;
import com.moustass.transactions_service.repository.MediaRepository;
import com.moustass.transactions_service.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private KmsClient kmsClient;

    @Mock
    private MinIOService minIOService;

    @Mock
    private UserClient userClient;

    @Mock
    private AuditClient auditClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction testTransaction;
    private Media testMedia;
    private TransactionRequestDto transactionRequestDto;
    private VerifyTransactionDto verifyTransactionDto;
    private MultipartFile mockVideoFile;
    private MultipartFile mockPrivateKeyFile;

    @BeforeEach
    void setUp() throws Exception {
        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setOwnerId(1L);
        testTransaction.setRecipientId(2L);
        testTransaction.setAmount(100.0);
        testTransaction.setStatus(TransactionStatus.PENDING);
        testTransaction.setCreatedAt(LocalDateTime.now());

        testMedia = new Media();
        testMedia.setId(1L);
        testMedia.setTransaction(testTransaction);
        testMedia.setObjectName("test-video.mp4");
        testMedia.setVideoHash("video-hash-base64");
        testMedia.setVideoSig("video-signature-base64");
        testMedia.setSignPk("public-key-base64");

        transactionRequestDto = new TransactionRequestDto();
        transactionRequestDto.setOwnerId(1L);
        transactionRequestDto.setRecipientId(2L);
        transactionRequestDto.setAmount(100.0);
        transactionRequestDto.setValidity(12);
        transactionRequestDto.setKeyId(1L);
        transactionRequestDto.setPublicKey("public-key-base64");

        mockVideoFile = mock(MultipartFile.class);
        transactionRequestDto.setVideo(mockVideoFile);

        mockPrivateKeyFile = mock(MultipartFile.class);
        transactionRequestDto.setSk(mockPrivateKeyFile);

        verifyTransactionDto = new VerifyTransactionDto();
        verifyTransactionDto.setTransactionId(1L);
        verifyTransactionDto.setPublicKey("public-key-base64");

    }

    @Test
    void testCreateTransaction_Success() throws Exception {
        // Arrange
        when(mockVideoFile.getBytes()).thenReturn("test video content".getBytes());
        when(mockPrivateKeyFile.getBytes()).thenReturn("private-key-content".getBytes());
        when(mockPrivateKeyFile.isEmpty()).thenReturn(false);
        when(kmsClient.signFile(anyLong(), anyLong(), anyString(), anyString())).thenReturn("signature-base64");
        when(minIOService.processMinIOStorage(any(MultipartFile.class))).thenReturn("storage-path");
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(mediaRepository.save(any(Media.class))).thenReturn(testMedia);

        // Act
        transactionService.createTransaction(transactionRequestDto);

        // Assert
        verify(transactionRepository).save(any(Transaction.class));
        verify(mediaRepository).save(any(Media.class));
        verify(kmsClient).signFile(anyLong(), anyLong(), anyString(), anyString());
        verify(minIOService).processMinIOStorage(any(MultipartFile.class));
        verify(auditClient).createAudit(any(AuditRequestDto.class));
        verify(notificationClient).createNotification(any(NotificationRequestDto.class));
    }

    @Test
    void testCreateTransaction_MissingPrivateKey() {
        // Arrange
        try {
            when(mockVideoFile.getBytes()).thenReturn("test video content".getBytes());
        } catch (Exception ignored) {}
        MultipartFile emptyFile = mock(MultipartFile.class);
        when(emptyFile.isEmpty()).thenReturn(true);
        transactionRequestDto.setSk(emptyFile);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.createTransaction(transactionRequestDto);
        });

        assertEquals("Fichier de clé privée manquant", exception.getMessage());
    }

    @Test
    void testVerifyTransaction_Success() throws Exception {
        // Arrange
        when(mediaRepository.findByTransactionId(1L)).thenReturn(Optional.of(testMedia));
        when(kmsClient.verifyFile("public-key-base64", "video-hash-base64", "video-signature-base64"))
                .thenReturn(true);
        doNothing().when(transactionRepository).updateStatus(1L, TransactionStatus.VERIFIED);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        // Act
        boolean result = transactionService.verifyTransaction(verifyTransactionDto);

        // Assert
        assertTrue(result);
        verify(mediaRepository).findByTransactionId(1L);
        verify(kmsClient).verifyFile(anyString(), anyString(), anyString());
        verify(transactionRepository).updateStatus(1L, TransactionStatus.VERIFIED);
        verify(auditClient).createAudit(argThat(dto -> 
            dto.getStatus().equals(AuditRequestDto.Status.SUCCES.name())
        ));
        verify(notificationClient).createNotification(any(NotificationRequestDto.class));
    }

    @Test
    void testVerifyTransaction_VerificationFailed() throws Exception {
        // Arrange
        when(mediaRepository.findByTransactionId(1L)).thenReturn(Optional.of(testMedia));
        when(kmsClient.verifyFile(anyString(), anyString(), anyString())).thenReturn(false);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        // Act
        boolean result = transactionService.verifyTransaction(verifyTransactionDto);

        // Assert
        assertFalse(result);
        verify(kmsClient).verifyFile(anyString(), anyString(), anyString());
        verify(auditClient).createAudit(argThat(dto -> 
            dto.getStatus().equals(AuditRequestDto.Status.FAILED.name())
        ));
    }

    @Test
    void testVerifyTransaction_MediaNotFound() {
        // Arrange
        when(mediaRepository.findByTransactionId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        GlobalException exception = assertThrows(GlobalException.class, () -> {
            transactionService.verifyTransaction(verifyTransactionDto);
        });

        assertTrue(exception.getMessage().contains("Media not found for transaction 1"));
        verify(mediaRepository).findByTransactionId(1L);
    }

    @Test
    void testGetTransactions_AsOwner() {
        // Arrange
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(testTransaction);

        when(transactionRepository.findByOwnerId(1L)).thenReturn(transactions);
        when(mediaRepository.findByTransactionId(1L)).thenReturn(Optional.of(testMedia));
        when(userClient.getUserName(2L)).thenReturn("recipient");

        // Act
        List<TransactionResponseDto> result = transactionService.getTransactions(1L, true);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("recipient", result.get(0).getUserName());
        verify(transactionRepository).findByOwnerId(1L);
        verify(transactionRepository, never()).findByRecipientId(anyLong());
    }

    @Test
    void testGetTransactions_AsRecipient() {
        // Arrange
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(testTransaction);

        when(transactionRepository.findByRecipientId(2L)).thenReturn(transactions);
        when(mediaRepository.findByTransactionId(1L)).thenReturn(Optional.of(testMedia));
        when(userClient.getUserName(1L)).thenReturn("owner");

        // Act
        List<TransactionResponseDto> result = transactionService.getTransactions(2L, false);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("owner", result.get(0).getUserName());
        verify(transactionRepository).findByRecipientId(2L);
        verify(transactionRepository, never()).findByOwnerId(anyLong());
    }

    @Test
    void testGetTransactions_MediaNotFound() {
        // Arrange
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(testTransaction);

        when(transactionRepository.findByOwnerId(1L)).thenReturn(transactions);
        when(mediaRepository.findByTransactionId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.getTransactions(1L, true);
        });

        assertEquals("Media not found for transaction 1", exception.getMessage());
    }
}
