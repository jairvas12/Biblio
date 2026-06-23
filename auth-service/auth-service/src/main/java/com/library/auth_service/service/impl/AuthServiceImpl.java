package com.library.auth_service.service.impl;

import com.library.auth_service.client.UserClient;
import com.library.auth_service.dto.AuthResponseDTO;
import com.library.auth_service.dto.LoginRequestDTO;
import com.library.auth_service.dto.RegisterRequestDTO;
import com.library.auth_service.dto.UserRequestDTO;
import com.library.auth_service.dto.UserResponseDTO;

import com.library.auth_service.exception.EmailAlreadyRegisteredException;
import com.library.auth_service.exception.InvalidCredentialsException;
import com.library.auth_service.exception.RegistrationProcessException;
import com.library.auth_service.exception.UserInactiveException;
import com.library.auth_service.exception.UserServiceUnavailableException;

import com.library.auth_service.dto.ChangeEmailRequestDTO;
import com.library.auth_service.dto.InternalEmailUpdateDTO;
import com.library.auth_service.exception.CredentialNotFoundException;
import com.library.auth_service.exception.EmailChangeProcessException;

import com.library.auth_service.model.AuthCredential;
import com.library.auth_service.repository.AuthCredentialRepository;

import com.library.auth_service.security.JwtService;
import com.library.auth_service.service.AuthService;

import feign.FeignException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {


    private final UserClient userClient;

    private final JwtService jwtService;

    private final PasswordEncoder passwordEncoder;

    private final AuthCredentialRepository credentialRepository;

    @Override
    @Transactional
    public AuthResponseDTO register(
            RegisterRequestDTO requestDTO
    ) {

        String normalizedEmail =
                normalizeEmail(requestDTO.getEmail());

        log.info(
                "Iniciando registro para el correo: {}",
                normalizedEmail
        );

        if (credentialRepository
                .existsByEmailIgnoreCase(normalizedEmail)) {

            log.warn(
                    "Registro rechazado. El correo ya posee credenciales: {}",
                    normalizedEmail
            );

            throw new EmailAlreadyRegisteredException(
                    "Ya existe una cuenta registrada con ese correo"
            );
        }

        UserRequestDTO userRequest =
                UserRequestDTO.builder()
                        .name(normalizeName(requestDTO.getName()))
                        .email(normalizedEmail)
                        .role("USER")
                        .build();

        UserResponseDTO createdUser;

        try {

            createdUser = userClient.createUser(userRequest);

        } catch (FeignException ex) {

            if (ex.status() == 400 || ex.status() == 409) {

                throw new EmailAlreadyRegisteredException(
                        "Ya existe un usuario registrado con ese correo"
                );
            }

            log.error(
                    "No fue posible contactar correctamente a user-service. Estado: {}",
                    ex.status()
            );

            throw new UserServiceUnavailableException(
                    "No fue posible completar el registro porque el servicio de usuarios no está disponible"
            );
        }

        try {

            AuthCredential credential =
                    AuthCredential.builder()
                            .userId(createdUser.getId())
                            .email(normalizedEmail)
                            .passwordHash(
                                    passwordEncoder.encode(
                                            requestDTO.getPassword()
                                    )
                            )
                            .build();

            credentialRepository.save(credential);

        } catch (RuntimeException ex) {

            compensateUserCreation(createdUser.getId());

            log.error(
                    "Error guardando las credenciales del usuario {}",
                    createdUser.getId(),
                    ex
            );

            throw new RegistrationProcessException(
                    "No fue posible guardar las credenciales del usuario"
            );
        }

        String token =
        jwtService.generateToken(
                createdUser.getId(),
                createdUser.getEmail(),
                createdUser.getRole()
        );

        log.info(
                "Usuario registrado correctamente con id: {}",
                createdUser.getId()
        );

        return buildAuthResponse(
                token,
                createdUser
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO login(
            LoginRequestDTO requestDTO
    ) {

        String normalizedEmail =
                normalizeEmail(requestDTO.getEmail());

        log.info(
                "Intento de inicio de sesión para: {}",
                normalizedEmail
        );

        AuthCredential credential =
                credentialRepository
                        .findByEmailIgnoreCase(normalizedEmail)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Login rechazado para correo no registrado: {}",
                                    normalizedEmail
                            );

                            return new InvalidCredentialsException(
                                    "Correo o contraseña incorrectos"
                            );
                        });

        boolean passwordMatches =
                passwordEncoder.matches(
                        requestDTO.getPassword(),
                        credential.getPasswordHash()
                );

        if (!passwordMatches) {

            log.warn(
                    "Login rechazado por contraseña incorrecta para: {}",
                    normalizedEmail
            );

            throw new InvalidCredentialsException(
                    "Correo o contraseña incorrectos"
            );
        }

        UserResponseDTO user;

        try {

            user = userClient.getUserByEmail(normalizedEmail);

        } catch (FeignException ex) {

            if (ex.status() == 404) {

                log.error(
                        "Existen credenciales en AUTH, pero el usuario no existe en USER: {}",
                        normalizedEmail
                );

                throw new InvalidCredentialsException(
                        "Correo o contraseña incorrectos"
                );
            }

            throw new UserServiceUnavailableException(
                    "No fue posible validar el estado del usuario"
            );
        }

        if (!Boolean.TRUE.equals(user.getActive())) {

            log.warn(
                    "Login rechazado porque el usuario está inactivo: {}",
                    normalizedEmail
            );

            throw new UserInactiveException(
                    "La cuenta se encuentra desactivada"
            );
        }

        String token =
        jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        log.info(
                "Inicio de sesión exitoso para el usuario id: {}",
                user.getId()
        );

        return buildAuthResponse(
                token,
                user
        );
    }

        @Override
        @Transactional
        public UserResponseDTO changeUserEmail(
                Long userId,
                ChangeEmailRequestDTO requestDTO
        ) {

        String normalizedEmail =
                normalizeEmail(requestDTO.getNewEmail());

        log.info(
                "Iniciando cambio coordinado de correo para usuario id: {}",
                userId
        );

        AuthCredential credential =
                credentialRepository.findByUserId(userId)
                        .orElseThrow(() ->
                                new CredentialNotFoundException(
                                        "No se encontraron credenciales para el usuario id: "
                                                + userId
                                )
                        );

        String previousEmail =
                credential.getEmail();

        if (previousEmail.equalsIgnoreCase(normalizedEmail)) {

                log.info(
                        "El nuevo correo coincide con el correo actual del usuario {}",
                        userId
                );

                return userClient.getUserByEmail(previousEmail);
        }

        boolean emailAlreadyUsed =
                credentialRepository
                        .existsByEmailIgnoreCaseAndUserIdNot(
                                normalizedEmail,
                                userId
                        );

        if (emailAlreadyUsed) {

                throw new EmailAlreadyRegisteredException(
                        "Ya existe una cuenta registrada con el correo: "
                                + normalizedEmail
                );
        }

        boolean userServiceWasUpdated = false;

        try {

                InternalEmailUpdateDTO internalRequest =
                        InternalEmailUpdateDTO.builder()
                                .newEmail(normalizedEmail)
                                .build();

                UserResponseDTO updatedUser =
                        userClient.updateEmailInternally(
                                userId,
                                internalRequest
                        );

                userServiceWasUpdated = true;

                credential.setEmail(normalizedEmail);

                /*
                * saveAndFlush fuerza la ejecución SQL ahora.
                * Así podemos detectar el error y compensar USER.
                */
                credentialRepository.saveAndFlush(credential);

                log.info(
                        "Correo actualizado correctamente para usuario id {}: {} -> {}",
                        userId,
                        previousEmail,
                        normalizedEmail
                );

                return updatedUser;

        } catch (FeignException.Conflict ex) {

                throw new EmailAlreadyRegisteredException(
                        "El correo ya pertenece a otro usuario: "
                                + normalizedEmail
                );

        } catch (FeignException.NotFound ex) {

                throw new CredentialNotFoundException(
                        "El usuario id " + userId
                                + " no existe en el servicio de usuarios"
                );

        } catch (FeignException ex) {

                log.error(
                        "Error comunicándose con USER-SERVICE durante el cambio de correo. Estado: {}",
                        ex.status(),
                        ex
                );

                throw new UserServiceUnavailableException(
                        "No fue posible actualizar el correo porque USER-SERVICE no está disponible"
                );

        } catch (RuntimeException ex) {

                log.error(
                        "Falló la actualización de auth_db para el usuario id {}",
                        userId,
                        ex
                );

                if (userServiceWasUpdated) {
                compensateEmailChange(
                        userId,
                        previousEmail
                );
                }

                throw new EmailChangeProcessException(
                        "No fue posible completar el cambio coordinado de correo"
                );
        }
        }

        private void compensateEmailChange(
                Long userId,
                String previousEmail
        ) {

        try {

                InternalEmailUpdateDTO compensationRequest =
                        InternalEmailUpdateDTO.builder()
                                .newEmail(previousEmail)
                                .build();

                userClient.updateEmailInternally(
                        userId,
                        compensationRequest
                );

                log.warn(
                        "Se restauró el correo anterior del usuario id {}: {}",
                        userId,
                        previousEmail
                );

        } catch (Exception ex) {

                log.error(
                        "ERROR CRÍTICO: no fue posible restaurar el correo del usuario id {}",
                        userId,
                        ex
                );
        }
        }

    private AuthResponseDTO buildAuthResponse(
            String token,
            UserResponseDTO user
    ) {

        return AuthResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    private void compensateUserCreation(
            Long userId
    ) {

        try {

            userClient.deactivateUser(userId);

            log.warn(
                    "Se desactivó el usuario {} porque no fue posible crear sus credenciales",
                    userId
            );

        } catch (Exception ex) {

            log.error(
                    "No fue posible compensar la creación del usuario {}",
                    userId,
                    ex
            );
        }
    }

    private String normalizeEmail(
            String email
    ) {

        return email.trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeName(
            String name
    ) {

        return name.trim()
                .replaceAll("\\s+", " ");
    }
}