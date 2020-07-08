package com.harlownk.easytodoj.api.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DbConnectionService {

    public static final String dbPropertiesLocation = "static/secrets/database.properties";

    private String connectionUrl;
    private String username;
    private String password;

    /**
     * Prepares the service to create database connections when require.
     * @throws IOException There must be a database properties file at dbPropertiesLocation with the required properties.
     */
    public DbConnectionService() throws IOException {
        Properties properties = new Properties();
        InputStream propInput = getClass().getClassLoader().getResourceAsStream(dbPropertiesLocation);
        if (propInput != null) {
            properties.load(propInput);
        } else {
            throw new FileNotFoundException("Can't find properties file for Database configuration.");
        }
        String host = properties.getProperty("host");
        String portNum = properties.getProperty("port");
        String databaseName = properties.getProperty("database");
        connectionUrl = "jdbc:postgresql://" + host + ":" + portNum + "/" + databaseName;
        username = properties.getProperty("username");
        password = properties.getProperty("password");
    }

    /**
     * Gets a new connection to the database at the location provided in the appropriate properties file at construction.
     * @return A Connection to the database.
     * @throws SQLException If there is an error creating the connection to the database, like wrong information in the
     *                      properties.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(connectionUrl, username, password);
    }


}
