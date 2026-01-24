package com.moustass.transactions_service.client.notification;

public class NotificationRequestDto {
    private Long ownerId;
    private Long recipeintId;
    private String action;

    public NotificationRequestDto(Long ownerId, Long recipeintId, String action) {
        this.ownerId = ownerId;
        this.recipeintId = recipeintId;
        this.action = action;
    }

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
