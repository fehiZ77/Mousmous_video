package com.moustass.transactions_service.controller;

import com.moustass.transactions_service.dto.TransactionRequestDto;
import com.moustass.transactions_service.dto.TransactionResponseDto;
import com.moustass.transactions_service.dto.VerifyTransactionDto;
import com.moustass.transactions_service.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private TransactionRequestDto transactionRequestDto;
    private TransactionResponseDto transactionResponseDto;
    private VerifyTransactionDto verifyTransactionDto;

    @BeforeEach
    void setUp() {
        transactionRequestDto = new TransactionRequestDto();
        transactionRequestDto.setOwnerId(1L);
        transactionRequestDto.setRecipientId(2L);
        transactionRequestDto.setAmount(100.0);

        transactionResponseDto = new TransactionResponseDto(
                1L,
                LocalDateTime.now(),
                "testuser",
                100.0,
                "video.mp4",
                "PENDING",
                "public-key"
        );

        verifyTransactionDto = new VerifyTransactionDto();
        verifyTransactionDto.setTransactionId(1L);
        verifyTransactionDto.setPublicKey("public-key");
    }

    @Test
    void testCreateTransaction_Success() throws Exception {
        // Arrange
        doNothing().when(transactionService).createTransaction(any(TransactionRequestDto.class));
        MultipartFile video = mock(MultipartFile.class);
        MultipartFile sk = mock(MultipartFile.class);
        when(sk.getOriginalFilename()).thenReturn("key.pem");

        // Act
        ResponseEntity<?> response = transactionController.create(1L, 2L, 100.0, 12, 1L, "public", video, sk);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Transaction saved", response.getBody());
        verify(transactionService).createTransaction(any(TransactionRequestDto.class));
    }

    @Test
    void testCreateTransaction_Failure() throws Exception {
        // Arrange
        doThrow(new Exception("Error")).when(transactionService).createTransaction(any(TransactionRequestDto.class));
        MultipartFile video = mock(MultipartFile.class);
        MultipartFile sk = mock(MultipartFile.class);
        when(sk.getOriginalFilename()).thenReturn("key.pem");

        // Act
        ResponseEntity<?> response = transactionController.create(1L, 2L, 100.0, 12, 1L, "public", video, sk);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error", response.getBody());
        verify(transactionService).createTransaction(any(TransactionRequestDto.class));
    }

    @Test
    void testGetTransactions_Success() throws Exception {
        // Arrange
        List<TransactionResponseDto> transactions = new ArrayList<>();
        transactions.add(transactionResponseDto);

        when(transactionService.getTransactions(1L, true)).thenReturn(transactions);

        // Act
        ResponseEntity<?> response = transactionController.getTransactions(true, 1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(transactionService).getTransactions(1L, true);
    }

    @Test
    void testGetTransactions_Failure() throws Exception {
        // Arrange
        when(transactionService.getTransactions(1L, true)).thenThrow(new RuntimeException("Error"));

        // Act
        ResponseEntity<?> response = transactionController.getTransactions(true, 1L);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error", response.getBody());
        verify(transactionService).getTransactions(1L, true);
    }

    @Test
    void testVerifyTransaction_Success() throws Exception {
        // Arrange
        when(transactionService.verifyTransaction(any(VerifyTransactionDto.class))).thenReturn(true);

        // Act
        ResponseEntity<?> response = transactionController.verifyTransaction(verifyTransactionDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
        verify(transactionService).verifyTransaction(any(VerifyTransactionDto.class));
    }

    @Test
    void testVerifyTransaction_Failure() throws Exception {
        // Arrange
        when(transactionService.verifyTransaction(any(VerifyTransactionDto.class))).thenThrow(new Exception("Error"));

        // Act
        ResponseEntity<?> response = transactionController.verifyTransaction(verifyTransactionDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error", response.getBody());
        verify(transactionService).verifyTransaction(any(VerifyTransactionDto.class));
    }
}
