package com.harlownk.easytodoj.api;

import com.harlownk.easytodoj.api.services.AuthService;
import com.harlownk.easytodoj.api.services.DbConnectionService;
import com.harlownk.easytodoj.api.services.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.sql.SQLException;

@Configuration
@ComponentScan("com.harlownk.easytodoj.api")
public class Config {

    @Bean
    public Logger logger() {
        return new Logger();
    }

    @Bean
    public AuthService authService() throws IOException, SQLException {
        return new AuthService(new DbConnectionService());
    }

    @Bean
    public DbConnectionService dbConnectionService() throws IOException {
        return new DbConnectionService();
    }

}
