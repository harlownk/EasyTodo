package com.harlownk.easytodoj.api.tasks;

import com.harlownk.easytodoj.api.auth.MessageCarriable;
import com.harlownk.easytodoj.api.services.AuthService;
import com.harlownk.easytodoj.api.services.DbConnectionService;
import com.harlownk.easytodoj.api.services.TaskService;
import com.nimbusds.jwt.JWTClaimsSet;
import com.vaadin.flow.server.frontend.TaskCreatePackageJson;
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

    public static final String USER_HEADER_KEY = "ETJ_Username";
    public static final String USER_AUTH_HEADER_KEY = "ETJ_User_Auth";

    @GetMapping("/api/tasks")
    public ResponseEntity<List<TaskResponse>> getUserTasks(@RequestHeader HttpHeaders header) {
        // Authenticate the user's request.
        String userId = header.getFirst(USER_HEADER_KEY);
        userId = "Hello ";
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        String userAuthKey = header.getFirst(USER_AUTH_HEADER_KEY);
        userAuthKey = "World!";
        if (userAuthKey == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }


        List<TaskResponse> result = new ArrayList<>();
        TaskResponse res = new TaskResponse();
        res.setUserId("harlow");
        Task task = new Task();
        task.setCompleted(false);
        task.setTimeCreated((new Date().getTime()) / 1000);
        task.setTaskId(1);
        res.setTask(task);
        result.add(res);
        result.add(res);
        result.add(res);
        return ResponseEntity.ok().body(result);
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
        // Get authentication from header.
        String authValue = header.getFirst("Authorization");
        if (authValue == null) {
            response.setMessage("Authorization Header missing.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        if (!"Bearer".equalsIgnoreCase(authService.getAuthType(authValue))) {
            response.setMessage("Improper Authorization type. Must be type 'Bearer' with a valid authToken provided from /api/auth.");
        }
        String userAuthToken = authService.getAuthCreds(authValue);
        // Authenticate the users token, checking for validity.
        if (!authService.verifyToken(userAuthToken)) {
            response.setMessage("Invalid/Expired token. Request a new token through the endpoint /api/auth/ or register through /api/auth/new");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } // We are authenticated, now we can perform the work of adding the task under the user.

        // Get the information needed to add a task.
        // Task info from body.
        Task taskToAdd = body.getTask();
        if (taskToAdd == null) {
            response.setMessage("No Task provided.");
            return ResponseEntity.ok(response);
        }
        // User information.
        JWTClaimsSet claims = authService.getClaimsFromAuthToken(userAuthToken);
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
