package com.library.find_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FindServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FindServiceApplication.class, args);
    }
}
