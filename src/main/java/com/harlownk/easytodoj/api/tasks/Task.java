package com.harlownk.easytodoj.api.tasks;

import java.util.Date;

public class Task {

    private long taskId;
    private String taskDescription;
    private boolean completed;
    private long timeCreated;
    private long timeDue;

    public void setTaskId(long taskId) {
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

    public long getTaskId() {
        return taskId;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public boolean getCompleted() {
        return completed;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public long getTimeDue() {
        return timeDue;
    }
}
