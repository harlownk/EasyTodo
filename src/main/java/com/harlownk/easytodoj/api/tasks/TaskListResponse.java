package com.harlownk.easytodoj.api.tasks;

import com.harlownk.easytodoj.api.auth.MessageCarriable;

import java.util.List;

public class TaskListResponse implements MessageCarriable {

    private String message;
    private long userId;
    private List<Task> taskList;

    @Override
    public String getMessage() {
        return message;
    }

    public long getUserId() {
        return userId;
    }

    public List<Task> getTaskList() {
        return taskList;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }
}
