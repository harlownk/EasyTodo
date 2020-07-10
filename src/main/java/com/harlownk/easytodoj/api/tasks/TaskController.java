package com.harlownk.easytodoj.api.tasks;

import com.harlownk.easytodoj.api.auth.MessageCarriable;
import com.harlownk.easytodoj.api.services.AuthService;
import com.harlownk.easytodoj.api.services.DbConnectionService;
import com.harlownk.easytodoj.api.services.TaskService;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class TaskController {

    private AuthService authService;
    private TaskService taskService;

    @Autowired
    public TaskController(AuthService authService, DbConnectionService connectionService) throws SQLException {
        this.authService = authService;
        this.taskService = new TaskService(connectionService);
    }

    @GetMapping("/api/tasks/all")
    public ResponseEntity<TaskListResponse> getUserTasks(@RequestHeader HttpHeaders header) {
        TaskListResponse response = new TaskListResponse();
        if (!authService.getAndAuthenticateToken(header, response)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        JWTClaimsSet claims = authService.getClaimsFromAuthToken(header);
        if (claims == null) {
            response.setMessage("Auth token doesn't contain the appropriate/neccessary claims.");
            return ResponseEntity.status(400).body(response);
        }
        long uid = (long) claims.getClaim("userId");
        List<Task> taskList = null;
        try {
             taskList = taskService.getTasksByUserId(uid);
        } catch (SQLException e) {
            response.setMessage("Error finding tasks in the database.");
            return ResponseEntity.status(500).body(response);
        }
        response.setMessage("Retrieved all tasks.");
        response.setTaskList(taskList);
        response.setUserId(uid);
        return ResponseEntity.ok().body(response);
    }

    /**
     * Do more error checking both here and in the TaskService. TODO
     * @param header
     * @param body
     * @return
     */
    @PostMapping(value="/api/tasks/add")
    public ResponseEntity<TaskResponse> addTask(@RequestHeader HttpHeaders header, @RequestBody TaskRequest body) {
        TaskResponse response = new TaskResponse();
        if (!authService.getAndAuthenticateToken(header, response)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        // Get the information needed to add a task.
        // Task info from body.
        Task taskToAdd = body.getTask();
        if (taskToAdd == null) {
            response.setMessage("No Task provided.");
            return ResponseEntity.ok(response);
        }
        // User information.
        JWTClaimsSet claims = authService.getClaimsFromAuthToken(header);
        if (claims == null) {
            response.setMessage("Auth token doesn't contain the appropriate/neccessary claims.");
            return ResponseEntity.status(400).body(response);
        }
        long uid = (long) claims.getClaim("userId");

        // Add to the database after getting information required to do so from the request.
        // Replying with errors if the data needed isn't in the request.
        int resultTid;
        try {
            resultTid = taskService.addTask(taskToAdd, uid);
        } catch (SQLException e) {
            e.printStackTrace();
            response.setMessage("Error adding task to the database.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        if (resultTid == -1) {
            response.setMessage("Error after trying to add task to the database.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        // With success adding the task to the
        Task responseTask = new Task();
        responseTask.setTimeCreated(new Date().getTime());
        responseTask.setTimeDue(taskToAdd.getTimeDue() );
        responseTask.setTaskDescription(taskToAdd.getTaskDescription());
        responseTask.setCompleted(responseTask.getCompleted());
        responseTask.setTaskId(resultTid); // ID From the database.

        response.setTask(responseTask);

        response.setMessage("Task added successfully.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/tasks/remove")
    public ResponseEntity removeTask(@RequestHeader HttpHeaders header, @RequestBody TaskRequest body) {
        return null;
    }

    @PutMapping("/api/tasks/update")
    public ResponseEntity updateTask(@RequestHeader HttpHeaders header, @RequestBody TaskRequest body) {
        return null;
    }


}
