package com.library.notification_service.config;

import com.library.notification_service.security.JwtAccessDeniedHandler;
import com.library.notification_service.security.JwtAuthenticationEntryPoint;
import com.library.notification_service.security.JwtAuthenticationFilter;
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

    private final JwtAuthenticationEntryPoint
            jwtAuthenticationEntryPoint;

    private final JwtAccessDeniedHandler
            jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf ->
                        csrf.disable()
                )

                .formLogin(form ->
                        form.disable()
                )

                .httpBasic(basic ->
                        basic.disable()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(
                                        jwtAuthenticationEntryPoint
                                )
                                .accessDeniedHandler(
                                        jwtAccessDeniedHandler
                                )
                )

                .authorizeHttpRequests(authorize ->
                        authorize

                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/v3/api-docs/**",
                                        "/error"
                                )
                                .permitAll()

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/notifications/**"
                                )
                                .hasAnyRole(
                                        "USER",
                                        "BIBLIOTECARIO",
                                        "ADMIN"
                                )

                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/notifications"
                                )
                                .hasAnyRole(
                                        "BIBLIOTECARIO",
                                        "ADMIN"
                                )

                                .requestMatchers(
                                        HttpMethod.PATCH,
                                        "/notifications/*/read"
                                )
                                .hasAnyRole(
                                        "USER",
                                        "BIBLIOTECARIO",
                                        "ADMIN"
                                )

                                .requestMatchers(
                                        HttpMethod.DELETE,
                                        "/notifications/*"
                                )
                                .hasRole("ADMIN")

                                .anyRequest()
                                .authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}