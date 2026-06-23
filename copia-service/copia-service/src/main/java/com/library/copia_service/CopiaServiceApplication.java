package com.library.copia_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class CopiaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CopiaServiceApplication.class, args);
    }
}