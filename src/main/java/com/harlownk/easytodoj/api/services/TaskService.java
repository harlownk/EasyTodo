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
        java.util.Date due = new java.util.Date(task.getTimeDue());

        java.sql.Date currDateSql = new java.sql.Date(now.getTime());
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

    public boolean updateTask(Task task, long userId) throws SQLException {
        PreparedStatement statement = dbConnection.prepareStatement("UPDATE public.tasks SET completed = ?, create_date = ?, due_date = ?, task_desc = ? WHERE tid = ? AND user_id = ? RETURNING *");
        statement.setBoolean(1, task.getCompleted());
        statement.setDate(2, new java.sql.Date(task.getTimeCreated()));
        statement.setDate(3, new java.sql.Date(task.getTimeDue()));
        statement.setString(4, task.getTaskDescription());
        statement.setLong(5, task.getTaskId());
        statement.setLong(6, userId);
        statement.execute();
        ResultSet set = statement.getResultSet();
        if (!set.next()) {
            set.close();
            statement.close();
            return false;
        }
        set.close();
        statement.close();
        return true;
    }

    public Task removeTask(long taskId, long userId) throws SQLException {
        Task result = new Task();
        // Begin a transaction in case there is an error in the deleting such as deleting more than one row.
        Statement transactionStatement = dbConnection.createStatement();
        transactionStatement.execute("BEGIN TRANSACTION");

        PreparedStatement statement = dbConnection.prepareStatement("DELETE FROM public.tasks WHERE tid = ? AND user_id = ? RETURNING *");
        statement.setLong(1, taskId);
        statement.setLong(2, userId);
        statement.execute();
        ResultSet set = statement.getResultSet();
        if (!set.next()) {  // if it didn't delete anything, that is fine: return an empty task (A task with all fields nulled).
            transactionStatement.execute("COMMIT");
            set.close();
            statement.close();
            return result;
        }

        result.setTaskId(set.getLong("tid"));
        result.setCompleted(set.getBoolean("completed"));
        result.setTaskDescription(set.getString("task_desc"));
        result.setTimeCreated(set.getDate("create_date").getTime());
        result.setTimeDue(set.getDate("due_date").getTime());

        // Clean up and return result.
        // Check if multiple rows returned from the delete query.
        if (set.next()) {  // I don't really know how to guarantee this will never happen, but since tid is a primary key it shouldn't
            transactionStatement.execute("ROLLBACK");
            set.close();
            statement.close();
            throw new IllegalStateException("Multiple rows found when selecting with primary key.");
        }
        transactionStatement.execute("COMMIT");
        set.close();
        statement.close();
        return result;
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
