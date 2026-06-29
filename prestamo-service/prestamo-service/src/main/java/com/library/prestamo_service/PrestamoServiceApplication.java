package com.library.prestamo_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class PrestamoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                PrestamoServiceApplication.class,
                args
        );
    }
}