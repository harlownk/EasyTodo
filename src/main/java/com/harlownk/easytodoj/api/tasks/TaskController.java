package com.harlownk.easytodoj.api.tasks;

import com.harlownk.easytodoj.api.services.AuthService;
import com.harlownk.easytodoj.api.services.DbConnectionService;
import com.harlownk.easytodoj.api.services.TaskService;
import com.harlownk.easytodoj.api.tasks.requests.TaskAddRequest;
import com.harlownk.easytodoj.api.tasks.requests.TaskRemoveRequest;
import com.harlownk.easytodoj.api.tasks.requests.TaskUpdateRequest;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.text.ParseException;
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
        List<Task> taskList;
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
    @PostMapping("/api/tasks/add")
    public ResponseEntity<TaskResponse> addTask(@RequestHeader HttpHeaders header, @RequestBody TaskAddRequest body) {
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
    public ResponseEntity<TaskResponse> removeTask(@RequestHeader HttpHeaders header, @RequestBody TaskRemoveRequest body) throws ParseException {
        TaskResponse response = new TaskResponse();
        if (!authService.getAndAuthenticateToken(header, response)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        // Get the information needed to add a task. In this case the task id.
        int tid = body.getTaskId();
        if (tid == 0) {
            response.setMessage("No Task id provided.");
            // TODO Decide the response code.
            return ResponseEntity.ok(response);
        }
        // Get the user id from the JWT Claims.
        JWTClaimsSet claims = authService.getClaimsFromAuthToken(header);
        if (claims == null) {
            response.setMessage("Given JWT couldn't find the needed claims.");
            return ResponseEntity.status(500).body(response);
        }
        long uid = claims.getLongClaim("userId");
        Task taskResult;
        try {
            taskResult = taskService.removeTask(tid, uid);
        } catch (SQLException e) {
            response.setMessage("Error removing task from the database.");
            return ResponseEntity.status(500).body(response);
        }
        if (taskResult.getTaskId() == 0) {
            response.setMessage("Task not reflected in the database.");
        } else {
            response.setMessage("Task removed successfully.");
        }
        response.setTask(taskResult);
        response.setUserId(uid);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/tasks/update")
    public ResponseEntity<TaskResponse> updateTask(@RequestHeader HttpHeaders header, @RequestBody TaskAddRequest body) {
        TaskResponse response = new TaskResponse();
        if (!authService.getAndAuthenticateToken(header, response)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        // Get the information needed to add a task.
        // Task info from body.
        Task task = body.getTask();
        if (task == null) {
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
        boolean updated;
        try {
            updated = taskService.updateTask(task, uid);
        } catch (SQLException e) {
            response.setMessage("Database error occurred when attempting");
            return ResponseEntity.status(500).body(response);
        }
        if (updated) {
            response.setMessage("Task successfully updated");
            response.setUserId(uid);
            return ResponseEntity.ok(response);
        } else {
            response.setMessage("Unable to update task.");
            return ResponseEntity.status(500).body(response);
        }


    }


}
