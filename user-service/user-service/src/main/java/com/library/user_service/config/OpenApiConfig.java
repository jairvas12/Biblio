package com.library.user_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;

import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Biblio - USER Service API",
                version = "1.0.0",
                description = """
                        Microservicio encargado de administrar los perfiles
                        de usuario de la plataforma bibliotecaria Biblio.

                        Incluye creación interna de usuarios, consultas,
                        actualización administrativa, desactivación lógica
                        y sincronización interna de correo con AUTH.
                        """,
                contact = @Contact(
                        name = "Equipo Biblio"
                )
        )
)
@SecuritySchemes({
        @SecurityScheme(
                name = "bearerAuth",
                type = SecuritySchemeType.HTTP,
                scheme = "bearer",
                bearerFormat = "JWT",
                description = """
                        Token JWT generado por AUTH.
                        Ingrese solamente el token,
                        sin escribir la palabra Bearer.
                        """
        ),
        @SecurityScheme(
                name = "internalApiKey",
                type = SecuritySchemeType.APIKEY,
                in = SecuritySchemeIn.HEADER,
                paramName = "X-Internal-API-Key",
                description = """
                        Credencial privada utilizada en las
                        comunicaciones internas entre AUTH y USER.
                        """
        )
})
public class OpenApiConfig {
}
