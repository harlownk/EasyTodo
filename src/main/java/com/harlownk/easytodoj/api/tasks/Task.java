package com.harlownk.easytodoj.api.tasks;

import java.util.Date;

public class Task {

    private String taskId;
    private String taskDescription;
    private boolean completed;
    private long timeCreated;
    private long timeDue;

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setTimeCreated(long creationTime) {
        this.timeCreated = creationTime;
    }

    public void setTimeDue(long dueTime) {
        this.timeDue = dueTime;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public boolean setCompleted() {
        return completed;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public long getTimeDue() {
        return timeDue;
    }
}
