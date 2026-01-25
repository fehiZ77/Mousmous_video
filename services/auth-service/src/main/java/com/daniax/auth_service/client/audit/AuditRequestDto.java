package com.daniax.auth_service.client.audit;

public class AuditRequestDto {
    private String service;
    private String actionName;
    private String actionDetails;
    private String status;
    private String date;
    private String token;

    public AuditRequestDto(String service, String actionName, String actionDetails, Status status, String date) {
        this.service = service;
        this.actionName = actionName;
        this.actionDetails = actionDetails;
        this.status = status.name();
        this.date = date;
    }

    public enum Status {
        SUCCES,
        FAILED
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getActionDetails() {
        return actionDetails;
    }

    public void setActionDetails(String actionDetails) {
        this.actionDetails = actionDetails;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
