package com.moustass.notification_service.dto;

public class NotificationResponseDto {
    private String detail;
    private String timePassed;

    public NotificationResponseDto(String detail, String timePassed) {
        this.detail = detail;
        this.timePassed = timePassed;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getTimePassed() {
        return timePassed;
    }

    public void setTimePassed(String timePassed) {
        this.timePassed = timePassed;
    }
}
