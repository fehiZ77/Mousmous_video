package com.moustass_video.kms_service.dto;

public class RevokeKeyRequestDto {
    private Long keyId;
    private Long userId;

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
