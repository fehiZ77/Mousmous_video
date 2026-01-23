package com.moustass.transactions_service.dto;

import org.springframework.web.multipart.MultipartFile;

public class TransactionRequestDto {
    private Long ownerId;
    private Long recipientId;
    private double amount;
    private int validity;
    private Long keyId;
    private String publicKey;
    private MultipartFile video;
    private MultipartFile sk;

    public TransactionRequestDto(
            Long ownerId,
            Long recipientId,
            double amount,
            int validity,
            Long keyId,
            String publicKey,
            MultipartFile video,
            MultipartFile sk
    ) {
        this.ownerId = ownerId;
        this.recipientId = recipientId;
        this.amount = amount;
        this.validity = validity;
        this.keyId = keyId;
        this.publicKey = publicKey;
        this.video = video;
        this.sk = sk;
    }

    public MultipartFile getSk() {
        return sk;
    }

    public void setSk(MultipartFile sk) {
        this.sk = sk;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getValidity() {
        return validity;
    }

    public void setValidity(int validity) {
        this.validity = validity;
    }

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public MultipartFile getVideo() {
        return video;
    }

    public void setVideo(MultipartFile video) {
        this.video = video;
    }
}
