package com.harlownk.easytodoj.api.services;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.postgresql.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.sql.Statement;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

public class AuthService {

    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH_BITS = 64 * 8;
    private static final int HASH_GEN_STRENGTH = 65536;
    private static final String SECRET_LOCATION = "static/secrets/secrets.properties";

    private static final int TOKEN_EXPR_TIME = 60 * 60 * 1000;  // One Hour = 60 * 60 * 1000.

    private SecureRandom random;
    private Connection dbConnection;
    private byte[] signingSecret;


    @Autowired
    public AuthService(DbConnectionService dbConnectionService) throws SQLException, IOException {
        random = new SecureRandom();
        dbConnection = dbConnectionService.getConnection();

        // Read the properties to get secrets for the application.
        Properties properties = new Properties();
        InputStream propInput = getClass().getClassLoader().getResourceAsStream(SECRET_LOCATION);
        if (propInput != null) {
            properties.load(propInput);
        } else {
            throw new FileNotFoundException("Can't find properties file containing secret for Token creation.");
        }
        String base64Secret = properties.getProperty("signSecret", null);
        if (base64Secret == null) {
            throw new RuntimeException("JWS Signing secret not provided.");
        }
        com.nimbusds.jose.util.Base64 signingSecret = com.nimbusds.jose.util.Base64.from(base64Secret);
        this.signingSecret = signingSecret.decode();
    }

    public boolean usernameExists(String username) throws SQLException {
        // Do we configure all our statements on construction? Or when we need them? Memory vs Speed.
        String statement = "SELECT COUNT(*) FROM public.users WHERE username LIKE ?";
        PreparedStatement preped = dbConnection.prepareStatement(statement);
        preped.setString(1, username);
        boolean result = !preped.execute();
        ResultSet results = preped.getResultSet();
        results.next();
        int count = results.getInt(1);
        results.close();
        preped.close();
        return count > 0;
    }

    /**
     * Adds a user to the database. Will always check for duplicate users through #usernameExists(), returning -1 on
     * username conflict, -2 on other issues.
     * @param username The username of the user we want to add.
     * @param password The password we will salt and hash in order to add.
     * @return the new unique id of the user after creation. -1 if an error occurs.
     * @throws SQLException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    public int addUser(String username, String password) throws SQLException, InvalidKeySpecException, NoSuchAlgorithmException {
        // Check for username conflicts.
        if (usernameExists(username)) {
            return -1;
        }
        // Get information
        byte[] salt = getNewSalt();
        byte[] passHash = generatePassHash(password, salt);

        String passHashEncoded = Base64.encodeBytes(passHash);
        String saltEncoded = Base64.encodeBytes(salt);

        // Prepare database query.
        PreparedStatement preparedStatement = dbConnection.prepareStatement("INSERT INTO public.users(id, username, password, salt) VALUES (DEFAULT, ?, ?, ?)");
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, passHashEncoded);
        preparedStatement.setString(3, saltEncoded);
        preparedStatement.execute();
        preparedStatement.close();
        return getUserId(username);

    }

    public boolean removeUser(String username, String password) throws SQLException, InvalidKeySpecException, NoSuchAlgorithmException {
        // Check if the user is in the database already.
        if (!usernameExists(username)) {
            return false;
        }
        // Check if user is authenticated to do this action
        int userId = authenticateUser(username, password);
        Statement transactionStatement = dbConnection.createStatement();
        transactionStatement.execute("BEGIN TRANSACTION");
        PreparedStatement removeStatement = dbConnection.prepareStatement("DELETE FROM public.users WHERE id = ?");
        removeStatement.setInt(1, userId);
        removeStatement.execute();
        int rowsDeleted = removeStatement.getUpdateCount();
        if (rowsDeleted != 1) {
            transactionStatement.execute("ROLLBACK TRANSACTION");
            return false;
        } else {
            transactionStatement.execute("COMMIT TRANSACTION");
            return true;
        }
    }

    /**
     * Confirms if the credentials provided are correct and provide authentication.
     * @param username
     * @param password
     * @return
     * @throws SQLException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    public int authenticateUser(String username, String password) throws SQLException, InvalidKeySpecException, NoSuchAlgorithmException {
        PreparedStatement getUserStatement = dbConnection.prepareStatement("SELECT * FROM public.users WHERE username LIKE ?");
        getUserStatement.setString(1, username);
        if (!getUserStatement.execute()) {
            getUserStatement.close();
            throw new SQLException("Error with the database.");
        }
        ResultSet set = getUserStatement.getResultSet();
        if (!set.next()) {
            return -1;  // The user name doesn't exist.
        }
        int userId = set.getInt("id");
        byte[] passwordHashed = Base64.decode(set.getString("password"));
        byte[] salt = Base64.decode(set.getString("salt"));

        byte[] providedPasswordHashed = generatePassHash(password, salt);

        boolean correctPassword = Arrays.equals(passwordHashed, providedPasswordHashed);

        set.close();
        if (correctPassword) {
            return userId;
        } else {
            return -1;  // The credentials are incorrect.
        }
    }

    /**
     * Generates a new authentication token for the user. It encodes and signs a new JWT auth token returning it as a
     * string encoded as Base64.  Sets claims as neccessary, the username, userId, and the permissions that the user
     * has been given.
     * @param username
     * @param userId
     * @param perms
     * @return
     * @throws JOSEException
     */
    public String generateAuthToken(String username, int userId, String perms) throws JOSEException {
        JWSSigner signer = new MACSigner(signingSecret);

        JWTClaimsSet.Builder payloadBuilder = new JWTClaimsSet.Builder();
        payloadBuilder.issuer("easytodo.gov.me.jk")
                .expirationTime(new Date(new Date().getTime() + TOKEN_EXPR_TIME))
                .issueTime(new Date())
                .claim("username", username)
                .claim("userId", userId)
                .claim("permissions", perms);

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.HS256).build(),
                payloadBuilder.build());

        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    /**
     * Verifies that the token provided is properly signed, belongs to the user, and is not expired.
     * @param jwtTokenString
     * @return
     */
    public boolean verifyToken(String jwtTokenString, String username) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(jwtTokenString);
            JWSVerifier verifier = new MACVerifier(signingSecret);
            if (!signedJWT.verify(verifier)) {
                return false;
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            if (!username.equals(claims.getClaim("username"))) {
                return false;
            }

            Date expiration = claims.getExpirationTime();
            if (expiration.before(new Date())) {
                return false;
            }
        } catch (ParseException | JOSEException e) {
            return false;
        }
        return true;
    }

    /**
     *  Creates a new standardized salt that we can use to salt and hash our password so we can securely
     *  store it in our database. The salt is Securely Randomly generated. The salt will be SALT_LENGTH bytes long.
     * @return A new random salt that is SALT_LENGTH bytes long.
     */
    private byte[] getNewSalt() {
        byte[] result = new byte[SALT_LENGTH];
        random.nextBytes(result);
        return result;
    }

    /**
     * Will generate the hash for the given password and salt so we can compare to what we have stored in the database,
     * or to add a new user to the database.
     * @param password The password that we want to hash.
     * @param salt Must be a salt generated with from this class' #getNewSalt() or found in the database.
     * @return the hash of the password after salting with the hash.
     * @throws NoSuchAlgorithmException If the runtime doesn't have the hash function available
     * @throws InvalidKeySpecException If the keyspec with the provided password and salt are invalid.
     */
    private byte[] generatePassHash(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] hashResult;
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, HASH_GEN_STRENGTH, HASH_LENGTH_BITS);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        hashResult = factory.generateSecret(spec).getEncoded();
        return hashResult;
    }

    // TODO
    public String getUsername(int id) throws SQLException {
        PreparedStatement getUsernamePrep = dbConnection.prepareStatement("SELECT username FROM public.users WHERE id = ?");
        getUsernamePrep.setInt(1, id);
        getUsernamePrep.execute();
        ResultSet set = getUsernamePrep.getResultSet();
        if (!set.next()) {
            return "";
        }
        String result = set.getString("username");
        set.close();
        getUsernamePrep.close();
        return result;
    }

    // TODO
    public int getUserId(String username) throws SQLException {
        PreparedStatement getUseridPrep = dbConnection.prepareStatement("SELECT id FROM public.users WHERE username = ?");
        getUseridPrep.setString(1, username);
        getUseridPrep.execute();
        ResultSet set = getUseridPrep.getResultSet();
        if (!set.next()) {
            return -1;
        }
        int result = set.getInt("id");
        set.close();
        getUseridPrep.close();
        return result;
    }

    public String getAuthType(String authValue) {
        String[] splitAuth = authValue.split(" ", 2);
        return splitAuth[0];
    }

    public String getAuthCreds(String authValue) {
        String[] splitAuth = authValue.split(" ", 2);
        if (splitAuth.length < 2) {
            return "";
        }
        return splitAuth[1];
    }
}
