package com.harlownk.easytodoj.api.services;

import com.harlownk.easytodoj.api.tasks.Task;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public List<Task> getTasksByUserId(long userId) throws SQLException {
        List<Task> taskList = new ArrayList<>();
        PreparedStatement statement = dbConnection.prepareStatement("SELECT * FROM public.tasks WHERE user_id = ?");
        statement.setLong(1, userId);
        statement.execute();
        ResultSet set = statement.getResultSet();
        while (set.next()) {
            Task currTask = new Task();
            currTask.setTaskId(set.getLong("tid"));
            currTask.setTaskDescription(set.getString("task_desc"));
            currTask.setCompleted(set.getBoolean("completed"));
            currTask.setTimeDue(set.getDate("due_date").getTime());
            currTask.setTimeCreated(set.getDate("create_date").getTime());
            taskList.add(currTask);
        }
        set.close();
        statement.close();
        return taskList;
    }

}
