package com.harlownk.easytodoj.api.tasks;

import com.harlownk.easytodoj.api.auth.MessageCarriable;

public class TaskResponse implements MessageCarriable {

    private String message;
    private String userId;
    private Task task;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
