package com.harlownk.easytodoj.api.services;

import com.harlownk.easytodoj.api.tasks.Task;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.*;

public class TaskService {

    private Connection dbConnection;

    @Autowired
    public TaskService(DbConnectionService connectionService) throws SQLException {
        dbConnection = connectionService.getConnection();
    }

    public int addTask(Task task, long userId) throws SQLException {
        java.util.Date now = new java.util.Date();
        java.sql.Date currDateSql = new java.sql.Date(now.getTime());
        java.util.Date due;
        if (task.getTimeDue() != 0) {
            due = new java.util.Date(task.getTimeDue());
        } else {
            due = new java.util.Date();
        }
        java.sql.Date dueDateSql = new java.sql.Date(due.getTime());
        PreparedStatement statement = dbConnection.prepareStatement("INSERT INTO public.tasks(tid, task_desc, completed, create_date, due_date, user_id) VALUES (DEFAULT, ?, ?, ?, ?, ?) RETURNING tid");
        statement.setString(1, task.getTaskDescription());
        statement.setBoolean(2, task.getCompleted());
        statement.setDate(3, currDateSql);
        statement.setDate(4, dueDateSql);
        statement.setLong(5, userId);
        statement.execute();
        ResultSet set = statement.getResultSet();
        if (!set.next()) {
            return -1;
        }
        int resultTid = set.getInt(1);
        set.close();
        statement.close();
        return resultTid;
    }

}
