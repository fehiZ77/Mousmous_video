package com.moustass_video.kms_service.dto;

public class GenerateKeyDto {
    private Long userId;
    private String keyName;
    private int validity;    // par mois ty

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public int getValidity() {
        return validity;
    }

    public void setValidty(int validity) {
        if(validity <= 0) {
            this.validity = 1;
        }else{
            this.validity = validity;
        }
    }
}
