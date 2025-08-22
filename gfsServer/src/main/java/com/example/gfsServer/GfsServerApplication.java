package com.example.gfsServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GfsServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(GfsServerApplication.class, args);
    }
}