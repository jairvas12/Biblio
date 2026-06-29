package com.library.inventory_service.config;

import com.library.inventory_service.security.JwtAccessDeniedHandler;
import com.library.inventory_service.security.JwtAuthenticationEntryPoint;
import com.library.inventory_service.security.JwtAuthenticationFilter;
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
                .csrf(csrf ->
                        csrf.disable()
                )

                .formLogin(form ->
                        form.disable()
                )

                .httpBasic(httpBasic ->
                        httpBasic.disable()
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
                                 * Swagger y documentación pública.
                                 */
                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/v3/api-docs/**",
                                        "/error"
                                )
                                .permitAll()

                                /*
                                 * Todos los usuarios autenticados
                                 * pueden consultar el inventario.
                                 */
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/inventory/**"
                                )
                                .hasAnyRole(
                                        "USER",
                                        "BIBLIOTECARIO",
                                        "ADMIN"
                                )

                                /*
                                 * Bibliotecarios y administradores
                                 * pueden registrar movimientos.
                                 */
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/inventory/movements"
                                )
                                .hasAnyRole(
                                        "BIBLIOTECARIO",
                                        "ADMIN"
                                )

                                /*
                                 * Solo ADMIN puede eliminar movimientos.
                                 */
                                .requestMatchers(
                                        HttpMethod.DELETE,
                                        "/inventory/movements/*"
                                )
                                .hasRole(
                                        "ADMIN"
                                )

                                /*
                                 * Cualquier otra ruta exige
                                 * autenticación válida.
                                 */
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