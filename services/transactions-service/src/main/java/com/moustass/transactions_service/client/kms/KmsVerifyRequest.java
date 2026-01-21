package com.moustass.transactions_service.client.kms;

public class KmsVerifyRequest {
    private String publicKey;
    private String fileHash;
    private String signature;

    public KmsVerifyRequest() {
    }

    public KmsVerifyRequest(String publicKey, String fileHash, String signature) {
        this.publicKey = publicKey;
        this.fileHash = fileHash;
        this.signature = signature;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
