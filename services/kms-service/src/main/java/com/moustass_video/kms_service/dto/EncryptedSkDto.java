package com.moustass_video.kms_service.dto;

public class EncryptedSkDto {
    private String encryptedData;
    private String iv;

    public EncryptedSkDto(String encryptedData, String iv) {
        this.encryptedData = encryptedData;
        this.iv = iv;
    }

    public String getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(String encryptedData) {
        this.encryptedData = encryptedData;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }
}
