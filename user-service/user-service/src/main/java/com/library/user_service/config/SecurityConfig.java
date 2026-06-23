package com.library.user_service.config;

import com.library.user_service.security.InternalApiKeyFilter;
import com.library.user_service.security.JwtAccessDeniedHandler;
import com.library.user_service.security.JwtAuthenticationEntryPoint;
import com.library.user_service.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter
            jwtAuthenticationFilter;

    private final InternalApiKeyFilter
            internalApiKeyFilter;

    private final JwtAuthenticationEntryPoint
            authenticationEntryPoint;

    private final JwtAccessDeniedHandler
            accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf ->
                        csrf.disable()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(
                                        authenticationEntryPoint
                                )
                                .accessDeniedHandler(
                                        accessDeniedHandler
                                )
                )

                .authorizeHttpRequests(auth ->
                        auth
                                /*
                                 * Documentación.
                                 */
                                .requestMatchers(
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html"
                                )
                                .permitAll()

                                /*
                                 * Rutas internas.
                                 * InternalApiKeyFilter comprueba la API Key.
                                 */
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/users"
                                )
                                .permitAll()

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/users/email/**"
                                )
                                .permitAll()

                                .requestMatchers(
                                        HttpMethod.DELETE,
                                        "/users/internal/*/deactivate"
                                )
                                .permitAll()

                                .requestMatchers(
                                        HttpMethod.PUT,
                                        "/users/internal/*/email"
                                )
                                .permitAll()                            

                                /*
                                 * Consulta de usuarios:
                                 * ADMIN y BIBLIOTECARIO.
                                 */
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/users",
                                        "/users/*"
                                )
                                .hasAnyRole(
                                        "ADMIN",
                                        "BIBLIOTECARIO"
                                )

                                /*
                                 * Modificación de usuarios:
                                 * solamente ADMIN.
                                 */
                                .requestMatchers(
                                        HttpMethod.PUT,
                                        "/users/*"
                                )

                                .hasRole("ADMIN")

                                .requestMatchers(
                                        HttpMethod.DELETE,
                                        "/users/*"
                                )
                                .hasRole("ADMIN")

                                .anyRequest()
                                .authenticated()
                )

                .addFilterBefore(
                        internalApiKeyFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .httpBasic(httpBasic ->
                        httpBasic.disable()
                )

                .formLogin(form ->
                        form.disable()
                );

        return http.build();
    }
}