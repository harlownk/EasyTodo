package com.harlownk.easytodoj.api.auth;

/**
 * Simple interface to represent a response/request that carries a string message.
 */
public interface MessageCarriable {

    void setMessage(String message);
    String getMessage();

}
