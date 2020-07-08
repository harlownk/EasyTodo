package com.harlownk.easytodoj.api.tasks;

import com.harlownk.easytodoj.api.services.AuthService;
import com.harlownk.easytodoj.api.services.DbConnectionService;
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
    private Connection dbConnection;

    @Autowired
    public TaskController(AuthService authService, DbConnectionService connectionService) throws SQLException {
        this.authService = authService;
        this.dbConnection = connectionService.getConnection();
    }

    public static final String USER_HEADER_KEY = "ETJ_Username";
    public static final String USER_AUTH_HEADER_KEY = "ETJ_User_Auth";

    @GetMapping("api/tasks")
    public ResponseEntity<List<TaskResponse>> getUserTasks(@RequestHeader HttpHeaders header) {
        // Authenticate the user's request.
        String userId = header.getFirst(USER_HEADER_KEY);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        String userAuthKey = header.getFirst(USER_AUTH_HEADER_KEY);
        if (userAuthKey == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        //
        List<TaskResponse> result = new ArrayList<>();
        TaskResponse res = new TaskResponse();
        res.setUserId("harlow");
        Task task = new Task();
        task.setCompleted(false);
        task.setTimeCreated(new Date().getTime());
        task.setTaskId("harlow1");
        res.setTask(task);
        result.add(res);
        result.add(res);
        result.add(res);
        return ResponseEntity.ok().body(result);
    }

    @PostMapping("api/tasks/add")
    public ResponseEntity<TaskResponse> addTask(@RequestHeader HttpHeaders header, @RequestBody TaskRequest body) {
        // Get user information
        String username = header.getFirst(USER_HEADER_KEY);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        String userAuthToken = header.getFirst(USER_AUTH_HEADER_KEY);
        if (userAuthToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        TaskResponse response = new TaskResponse();
        // Authenticate the users token.
        if (!authService.verifyToken(userAuthToken, username)) {
            response.setMessage("Invalid/Expired token. Request a new token through the endpoint /api/auth/ or register through /api/auth/new");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } // We are authenticated, now we can perform the work of adding the task under the user.
        Task taskToAdd = body.getTask();
        if (taskToAdd == null) {
            response.setMessage("No Task provided.");
            return ResponseEntity.ok(response);
        }
        long dateTime = taskToAdd.getTimeDue();
        Date date = new Date(dateTime);
        response.setMessage("Task added successfully.");

        response.setTask(taskToAdd);

        return ResponseEntity.ok(response);
    }

    @PostMapping("api/tasks/remove")
    public ResponseEntity removeTask(@RequestHeader HttpHeaders header, @RequestBody TaskRequest body) {
        return null;
    }

    @PutMapping("api/tasks/update")
    public ResponseEntity updateTask(@RequestHeader HttpHeaders header, @RequestBody TaskRequest body) {
        return null;
    }
}
