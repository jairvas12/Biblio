package com.library.copia_service.config;

import com.library.copia_service.security.JwtAccessDeniedHandler;
import com.library.copia_service.security.JwtAuthenticationEntryPoint;
import com.library.copia_service.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint authenticationEntryPoint,
            JwtAccessDeniedHandler accessDeniedHandler
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

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
                                .requestMatchers(
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html"
                                )
                                .permitAll()

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/copias/**"
                                )
                                .hasAnyRole(
                                        "ADMIN",
                                        "BIBLIOTECARIO",
                                        "USER"
                                )

                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/copias"
                                )
                                .hasAnyRole(
                                        "ADMIN",
                                        "BIBLIOTECARIO"
                                )

                                .requestMatchers(
                                        HttpMethod.PUT,
                                        "/copias/*"
                                )
                                .hasAnyRole(
                                        "ADMIN",
                                        "BIBLIOTECARIO"
                                )

                                .requestMatchers(
                                        HttpMethod.PATCH,
                                        "/copias/*/estado"
                                )
                                .hasAnyRole(
                                        "ADMIN",
                                        "BIBLIOTECARIO"
                                )

                                .requestMatchers(
                                        HttpMethod.DELETE,
                                        "/copias/*"
                                )
                                .hasRole("ADMIN")

                                .anyRequest()
                                .authenticated()
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