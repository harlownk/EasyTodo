package com.harlownk.easytodoj.api.services;

import com.harlownk.easytodoj.api.tasks.Task;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TaskService {

    private Connection dbConnection;

    @Autowired
    public TaskService(DbConnectionService connectionService) throws SQLException {
        dbConnection = connectionService.getConnection();
    }

    public void addTask(Task task, int userId) throws SQLException {
        java.util.Date now = new java.util.Date();
        java.sql.Date currDateSql = new java.sql.Date(now.getTime());
        java.util.Date due;
        if (task.getTimeDue() != 0) {
            due = new java.util.Date(task.getTimeDue());
        } else {
            due = new java.util.Date();
        }
        java.sql.Date dueDateSql = new java.sql.Date(due.getTime());
        PreparedStatement statement = dbConnection.prepareStatement("INSERT INTO public.tasks(tid, user_id, task_desc, completed, create_date, due_date) VALUES (DEFAULT, ?, ?, ?, ?, ?)");
        statement.setInt(1, userId);
        statement.setString(2, task.getTaskDescription());
        statement.setBoolean(3, task.getCompleted());
        statement.setDate(4, currDateSql);
        statement.setDate(5, dueDateSql);
        statement.executeUpdate();
    }

}
