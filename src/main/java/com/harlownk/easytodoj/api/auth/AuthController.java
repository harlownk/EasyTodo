package com.harlownk.easytodoj.api.auth;

import com.harlownk.easytodoj.api.services.AuthService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

@RestController
public class AuthController {

    private AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/auth")
    public ResponseEntity<LoginResponse> authenticateUser(@RequestHeader HttpHeaders header) {
        // Get the information we care about from the header.
        LoginResponse response = new LoginResponse();
        // Parse the header to get to the username and password.
        Credentials creds = getCredentialsFromAuthValue(header, response);
        if (creds == null) {
            return ResponseEntity.status(409).body(response);
        }
        String username = creds.username;
        String password = creds.password;
        try {
            // Check if the credentials are valid.
            int userId = authService.authenticateUser(username, password);
            if (userId != -1) {
                response.setMessage("Login successful");
                response.setUserId(userId);
                response.setUsername(username);
                response.setAuthToken(authService.generateAuthToken(username, userId, "user"));
                return ResponseEntity.ok().body(response);
            } else {
                response.setMessage("Credentials Invalid.");
                return ResponseEntity.ok().body(response);
            }
        } catch (SQLException e) {
            response.setMessage("Database Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (InvalidKeySpecException | JOSEException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/api/auth/new")
    public ResponseEntity<SignUpResponse> createUser(@RequestHeader HttpHeaders header) {
        // Get the information we care about from the header.
        SignUpResponse response = new SignUpResponse();
        // Parse the header to get to the username and password.
        Credentials creds = getCredentialsFromAuthValue(header, response);
        if (creds == null) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(response);
        }
        String username = creds.username;
        String password = creds.password;

        try {
            // Salt and Hash the password, add everything to the database.
            int userId = authService.addUser(username, password);
            if (userId == -1) {
                response.setMessage("Username Taken");
                return ResponseEntity.ok().body(response);
            } else if (userId == -2) {
                response.setMessage("Error checking for username conflicts.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            // Get the users auth token to return.
            String jwtToken = authService.generateAuthToken(username, userId, "user");
            // Construct and send response.
            response.setMessage("User successfully created.");
            response.setUsername(username);
            response.setUserId(userId);
            response.setPermissionsGranted("user");
            response.setAuthToken(jwtToken);
            return ResponseEntity.ok().body(response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.setMessage("Database Error.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            response.setMessage("Error creating user.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (JOSEException e) {
            response.setMessage("Error generating authentication token.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/api/auth/delete")
    public ResponseEntity<RemoveUserResponse> deleteUser(@RequestHeader HttpHeaders header) {
        // Get the information we care about from the header.
        RemoveUserResponse response = new RemoveUserResponse();
        // Parse the header to get to the username and password.
        Credentials creds = getCredentialsFromAuthValue(header, response);
        if (creds == null) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(response);
        }
        String username = creds.username;
        String password = creds.password;
        try {
            if (authService.removeUser(username, password)) {
                response.setMessage("User successfully removed.");
                return ResponseEntity.ok().body(response);
            } else {
                response.setMessage("Error in removing user from database.");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
        } catch (SQLException e) {
            response.setMessage("Database Error.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            response.setMessage("Error creating user.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/api/auth/print")
    public ResponseEntity<String> printRequest(@RequestHeader HttpHeaders header) {
        return ResponseEntity.ok(header.toString());
    }

    /**
     *
     * @param header
     * @param response
     * @return
     */
    private Credentials getCredentialsFromAuthValue(HttpHeaders header, MessageCarriable response) {
        // Parse the header to get to the username and password.
        String authValue = header.getFirst("Authorization");
        String authType = authService.getAuthType(authValue);
        if (!"Basic".toLowerCase().equalsIgnoreCase(authType)) {
            response.setMessage("Improper Authorization type.");
            return null;
        }
        String credsBase64 = authService.getAuthCreds(authValue);
        Base64 encodedCreds = Base64.from(credsBase64);
        if ("".equals(credsBase64)) {
            response.setMessage("Credentials not provided in Authorization.");
            return null;
        }
        String decoded = new String(encodedCreds.decode(), StandardCharsets.US_ASCII);
        String[] credPair = decoded.split(":", 2);
        if (credPair.length != 2) {
            response.setMessage("Credentials have improper format.");
            return null;
        }
        Credentials creds = new Credentials();
        creds.username = credPair[0];
        creds.password = credPair[1];
        return creds;
    }

    private static class Credentials {
        public String username;
        public String password;
    }

}
