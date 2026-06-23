package com.library.auth_service.config;

import feign.RequestInterceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserClientConfig {

    @Bean
    public RequestInterceptor internalApiKeyInterceptor(
            @Value("${internal.api.key}")
            String internalApiKey
    ) {

        return requestTemplate ->
                requestTemplate.header(
                        "X-Internal-API-Key",
                        internalApiKey
                );
    }
}
