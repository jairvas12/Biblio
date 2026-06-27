package com.library.auth_service.controller;

import com.library.auth_service.dto.AuthResponseDTO;
import com.library.auth_service.dto.ChangeEmailRequestDTO;
import com.library.auth_service.dto.LoginRequestDTO;
import com.library.auth_service.dto.RegisterRequestDTO;
import com.library.auth_service.dto.UserResponseDTO;
import com.library.auth_service.service.AuthService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void register_shouldReturnCreatedResponse() {

        RegisterRequestDTO requestDTO =
                new RegisterRequestDTO();

        requestDTO.setName(
                "Usuario Nuevo"
        );

        requestDTO.setEmail(
                "nuevo@biblio.cl"
        );

        requestDTO.setPassword(
                "ClaveSegura123"
        );

        AuthResponseDTO serviceResponse =
                AuthResponseDTO.builder()
                        .token("jwt-registro")
                        .tokenType("Bearer")
                        .userId(10L)
                        .email("nuevo@biblio.cl")
                        .role("USER")
                        .build();

        when(
                authService.register(requestDTO)
        ).thenReturn(serviceResponse);

        ResponseEntity<AuthResponseDTO> response =
                authController.register(requestDTO);

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        assertSame(
                serviceResponse,
                response.getBody()
        );

        assertEquals(
                "jwt-registro",
                response.getBody().getToken()
        );

        verify(authService).register(
                requestDTO
        );
    }

    @Test
    void login_shouldReturnOkResponse() {

        LoginRequestDTO requestDTO =
                new LoginRequestDTO();

        requestDTO.setEmail(
                "usuario@biblio.cl"
        );

        requestDTO.setPassword(
                "ClaveSegura123"
        );

        AuthResponseDTO serviceResponse =
                AuthResponseDTO.builder()
                        .token("jwt-login")
                        .tokenType("Bearer")
                        .userId(11L)
                        .email("usuario@biblio.cl")
                        .role("USER")
                        .build();

        when(
                authService.login(requestDTO)
        ).thenReturn(serviceResponse);

        ResponseEntity<AuthResponseDTO> response =
                authController.login(requestDTO);

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertSame(
                serviceResponse,
                response.getBody()
        );

        assertEquals(
                "jwt-login",
                response.getBody().getToken()
        );

        verify(authService).login(
                requestDTO
        );
    }

    @Test
    void changeUserEmail_shouldReturnUpdatedUser() {

        Long userId = 12L;

        ChangeEmailRequestDTO requestDTO =
                new ChangeEmailRequestDTO();

        requestDTO.setNewEmail(
                "nuevo.correo@biblio.cl"
        );

        UserResponseDTO updatedUser =
                UserResponseDTO.builder()
                        .id(userId)
                        .name("Usuario Actualizado")
                        .email("nuevo.correo@biblio.cl")
                        .role("USER")
                        .active(true)
                        .build();

        when(
                authService.changeUserEmail(
                        userId,
                        requestDTO
                )
        ).thenReturn(updatedUser);

        ResponseEntity<UserResponseDTO> response =
                authController.changeUserEmail(
                        userId,
                        requestDTO
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertSame(
                updatedUser,
                response.getBody()
        );

        assertEquals(
                "nuevo.correo@biblio.cl",
                response.getBody().getEmail()
        );

        verify(authService).changeUserEmail(
                userId,
                requestDTO
        );
    }
}