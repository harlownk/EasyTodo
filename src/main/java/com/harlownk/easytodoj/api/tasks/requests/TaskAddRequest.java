package com.harlownk.easytodoj.api.tasks.requests;

import com.harlownk.easytodoj.api.tasks.Task;

public class TaskAddRequest {

    private Task task;

    public void setTask(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }


}
