package com.harlownk.easytodoj.api.auth;

public class RemoveUserResponse implements MessageCarriable {

    private String message;

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
