package com.daniax.auth_service.dto;

public class AuthResponse {
    private String token;
    private String userName;
    private String role;
    private boolean isFirstLogin;

    public AuthResponse() {
    }

    public AuthResponse(String token, String userName, String role, boolean isFirstLogin) {
        this.token = token;
        this.userName = userName;
        this.role = role;
        this.isFirstLogin = isFirstLogin;
    }

    public boolean isFirstLogin() {
        return isFirstLogin;
    }

    public void setFirstLogin(boolean firstLogin) {
        isFirstLogin = firstLogin;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
