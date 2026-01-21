package com.moustass.transactions_service.dto;

import java.time.LocalDateTime;

public class TransactionResponseDto {
    private Long transactionId;
    private LocalDateTime date;
    private String userName;
    private double amount;
    private String objectName;
    private String status;
    private String publicKey;

    public TransactionResponseDto(
            Long transactionId,
            LocalDateTime date,
            String userName,
            double amount,
            String urlVideo,
            String status,
            String pk
    ) {
        this.transactionId = transactionId;
        this.date = date;
        this.userName = userName;
        this.amount = amount;
        this.objectName = urlVideo;
        this.status = status;
        this.publicKey = pk;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
