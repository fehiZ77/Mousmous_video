package com.moustass_video.kms_service.dto;

import com.moustass_video.kms_service.entity.UserKeys;

public class GeneratedKeyResultDto {
    private UserKeys savedKey;
    private String privateKey;

    public GeneratedKeyResultDto(String privateKey, UserKeys savedKey) {
        this.privateKey = privateKey;
        this.savedKey = savedKey;
    }

    public UserKeys getSavedKey() {
        return savedKey;
    }

    public void setSavedKey(UserKeys savedKey) {
        this.savedKey = savedKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
