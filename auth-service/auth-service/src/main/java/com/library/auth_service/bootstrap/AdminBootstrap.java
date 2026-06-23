package com.library.auth_service.bootstrap;

import com.library.auth_service.client.UserClient;
import com.library.auth_service.dto.UserRequestDTO;
import com.library.auth_service.dto.UserResponseDTO;
import com.library.auth_service.model.AuthCredential;
import com.library.auth_service.repository.AuthCredentialRepository;

import feign.FeignException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final UserClient userClient;

    private final AuthCredentialRepository credentialRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.enabled:false}")
    private boolean bootstrapEnabled;

    @Value("${app.bootstrap.admin.name:}")
    private String adminName;

    @Value("${app.bootstrap.admin.email:}")
    private String adminEmail;

    @Value("${app.bootstrap.admin.password:}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {

        if (!bootstrapEnabled) {

            log.info(
                    "Bootstrap de administrador deshabilitado"
            );

            return;
        }

        String normalizedEmail =
                normalizeEmail(adminEmail);

        String normalizedName =
                normalizeName(adminName);

        if (normalizedName.isBlank()
                || normalizedEmail.isBlank()
                || adminPassword == null
                || adminPassword.isBlank()) {

            log.error(
                    "No se puede ejecutar el bootstrap: faltan datos del administrador"
            );

            return;
        }

        if (!isValidPassword(adminPassword)) {

            log.error(
                    "La contraseña del administrador debe tener entre 8 y 72 caracteres, " +
                    "incluyendo mayúscula, minúscula y número"
            );

            return;
        }

        if (credentialRepository
                .existsByEmailIgnoreCase(normalizedEmail)) {

            log.info(
                    "El administrador ya posee credenciales: {}",
                    normalizedEmail
            );

            return;
        }

        UserResponseDTO adminUser;

        boolean userCreatedDuringBootstrap = false;

        try {

            adminUser =
                    userClient.getUserByEmail(
                            normalizedEmail
                    );

            if (!"ADMIN".equalsIgnoreCase(
                    adminUser.getRole()
            )) {

                log.error(
                        "El correo {} ya pertenece a un usuario que no tiene rol ADMIN",
                        normalizedEmail
                );

                return;
            }

            if (!Boolean.TRUE.equals(
                    adminUser.getActive()
            )) {

                log.error(
                        "El usuario administrador {} está desactivado",
                        normalizedEmail
                );

                return;
            }

        } catch (FeignException.NotFound ex) {

            log.info(
                    "Creando usuario administrador inicial: {}",
                    normalizedEmail
            );

            UserRequestDTO request =
                    UserRequestDTO.builder()
                            .name(normalizedName)
                            .email(normalizedEmail)
                            .role("ADMIN")
                            .build();

            adminUser =
                    userClient.createUser(request);

            userCreatedDuringBootstrap = true;

        } catch (FeignException ex) {

            log.error(
                    "No fue posible comunicarse con USER-SERVICE. Estado HTTP: {}",
                    ex.status(),
                    ex
            );

            return;
        }

        try {

            AuthCredential credential =
                    AuthCredential.builder()
                            .userId(adminUser.getId())
                            .email(normalizedEmail)
                            .passwordHash(
                                    passwordEncoder.encode(
                                            adminPassword
                                    )
                            )
                            .build();

            credentialRepository.save(credential);

            log.warn(
                    "Administrador inicial creado correctamente: {}. " +
                    "Deshabilite inmediatamente el bootstrap.",
                    normalizedEmail
            );

        } catch (RuntimeException ex) {

            log.error(
                    "No fue posible guardar las credenciales del administrador",
                    ex
            );

            if (userCreatedDuringBootstrap) {

                compensateUserCreation(
                        adminUser.getId()
                );
            }
        }
    }

    private void compensateUserCreation(
            Long userId
    ) {

        try {

            userClient.deactivateUser(userId);

            log.warn(
                    "El usuario administrador {} fue desactivado porque falló la creación de credenciales",
                    userId
            );

        } catch (Exception ex) {

            log.error(
                    "No fue posible compensar la creación del administrador {}",
                    userId,
                    ex
            );
        }
    }

    private boolean isValidPassword(
            String password
    ) {

        return password.matches(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,72}$"
        );
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null) {
            return "";
        }

        return email.trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeName(
            String name
    ) {

        if (name == null) {
            return "";
        }

        return name.trim()
                .replaceAll("\\s+", " ");
    }
}