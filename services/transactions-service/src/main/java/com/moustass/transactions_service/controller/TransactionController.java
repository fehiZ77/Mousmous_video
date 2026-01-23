package com.moustass.transactions_service.controller;

import com.moustass.transactions_service.client.minio.MinIOService;
import com.moustass.transactions_service.dto.TransactionRequestDto;
import com.moustass.transactions_service.dto.VerifyTransactionDto;
import com.moustass.transactions_service.service.TransactionService;
import io.minio.StatObjectResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final MinIOService minIOService;

    public TransactionController(TransactionService transactionService, MinIOService minIOService) {
        this.transactionService = transactionService;
        this.minIOService = minIOService;
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyTransaction(@RequestBody VerifyTransactionDto dto){
        try {
            return new ResponseEntity<>(transactionService.verifyTransaction(dto), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("/videos/{objectName}")
    public ResponseEntity<?> streamVideo(@PathVariable String objectName) {
        try {
            // Récupère le stream depuis MinIO
            Resource resource = minIOService.getInputStream(objectName);

            // Récupère les métadonnées pour le content-type
            StatObjectResponse stat =minIOService.getStatObject(objectName);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(stat.contentType()))
                    .contentLength(stat.size())
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + objectName + "\"")
                    .body(resource);

        } catch (Exception e) {
            return new ResponseEntity<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getTransactions(
            @RequestParam("isOwner") boolean isOwner,
            @RequestParam("userId") Long userId,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.substring(7);
        try {
            return new ResponseEntity<>(transactionService.getTransactions(userId, token, isOwner), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> create(
            @RequestParam("owner_id") Long ownerId,
            @RequestParam("recipient_id") Long recipientId,
            @RequestParam("amount") Double amount,
            @RequestParam("validity") Integer validity,
            @RequestParam("keyId") Long keyId,
            @RequestParam("publicKey") String publicKey,
            @RequestPart("video") MultipartFile video,
            @RequestPart("sk") MultipartFile sk,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String filename = sk.getOriginalFilename();
            if (filename == null || !filename.toLowerCase().endsWith(".pem")) {
                return new ResponseEntity<>(
                        "Seuls les fichiers .pem sont acceptés",
                        HttpStatus.BAD_REQUEST
                );
            }
            TransactionRequestDto request = new TransactionRequestDto(
                    ownerId,
                    recipientId,
                    amount,
                    validity,
                    keyId,
                    publicKey,
                    video,
                    sk
            );

            transactionService.createTransaction(request);
            return new ResponseEntity<>("Transaction saved", HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
