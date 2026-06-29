package com.library.prestamo_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                /*
                 * Se desactiva CSRF porque el microservicio
                 * expone una API REST sin sesiones web.
                 */
                .csrf(AbstractHttpConfigurer::disable)

                /*
                 * Se desactivan los mecanismos automáticos
                 * de formulario y autenticación básica.
                 */
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                /*
                 * La API funcionará sin sesiones almacenadas
                 * en el servidor.
                 */
                .sessionManagement(
                        session -> session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                /*
                 * Configuración temporal para pruebas.
                 */
                .authorizeHttpRequests(
                        authorization -> authorization

                                /*
                                 * Permite acceder a Swagger y OpenAPI.
                                 */
                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/v3/api-docs/**",
                                        "/error"
                                )
                                .permitAll()

                                /*
                                 * Permite temporalmente probar todos
                                 * los endpoints de préstamos.
                                 */
                                .requestMatchers(
                                        "/prestamos/**"
                                )
                                .permitAll()

                                /*
                                 * Cualquier otra ruta queda bloqueada.
                                 */
                                .anyRequest()
                                .denyAll()
                );

        return http.build();
    }
}