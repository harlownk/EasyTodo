package com.harlownk.easytodoj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class EasytodojApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasytodojApplication.class, args);
    }
    

}
