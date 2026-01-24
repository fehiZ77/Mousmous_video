package com.moustass.notification_service.dto;

public class NotificationRequestDto {
    private Long ownerId;
    private Long recipeintId;
    private String action;

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getRecipeintId() {
        return recipeintId;
    }

    public void setRecipeintId(Long recipeintId) {
        this.recipeintId = recipeintId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
