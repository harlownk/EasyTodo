package com.harlownk.easytodoj.api.auth;

public class SignUpResponse implements MessageCarriable {

    private String message;
    private String username;
    private int userId;
    private String authToken;
    private String permissionsGranted;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getPermissionsGranted() {
        return permissionsGranted;
    }

    public void setPermissionsGranted(String permissionsGranted) {
        this.permissionsGranted = permissionsGranted;
    }
}
