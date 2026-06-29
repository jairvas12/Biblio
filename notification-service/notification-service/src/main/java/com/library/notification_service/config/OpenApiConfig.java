package com.library.notification_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME =
            "bearerAuth";

    @Bean
    public OpenAPI notificationOpenAPI() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title(
                                        "Biblio - NOTIFICATION Service API"
                                )
                                .description(
                                        "API para la gestión de notificaciones de la biblioteca"
                                )
                                .version("1.0.0")
                )
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(
                                        SECURITY_SCHEME_NAME
                                )
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        SECURITY_SCHEME_NAME,
                                        new SecurityScheme()
                                                .name(
                                                        SECURITY_SCHEME_NAME
                                                )
                                                .type(
                                                        SecurityScheme.Type.HTTP
                                                )
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                );
    }
}