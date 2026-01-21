package com.moustass.transactions_service.dto;

import org.springframework.web.multipart.MultipartFile;

public class TransactionRequestDto {
    private Long owner_id;
    private Long recipient_id;
    private double amount;
    private int validity;
    private Long keyId;
    private String publicKey;
    private MultipartFile video;
    private String token;

    public TransactionRequestDto(Long owner_id, Long recipient_id, double amount, int validity, Long keyId, String publicKey, MultipartFile video, String token) {
        this.owner_id = owner_id;
        this.recipient_id = recipient_id;
        this.amount = amount;
        this.validity = validity;
        this.keyId = keyId;
        this.publicKey = publicKey;
        this.video = video;
        this.token = token;
    }

    public Long getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(Long owner_id) {
        this.owner_id = owner_id;
    }

    public Long getRecipient_id() {
        return recipient_id;
    }

    public void setRecipient_id(Long recipient_id) {
        this.recipient_id = recipient_id;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
