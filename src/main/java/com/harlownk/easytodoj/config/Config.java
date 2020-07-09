package com.harlownk.easytodoj.config;

import com.harlownk.easytodoj.api.services.AuthService;
import com.harlownk.easytodoj.api.services.DbConnectionService;
import com.harlownk.easytodoj.api.services.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedOrigins("*")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }


}
