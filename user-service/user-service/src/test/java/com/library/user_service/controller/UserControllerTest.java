package com.library.user_service.controller;

import com.library.user_service.dto.InternalEmailUpdateDTO;
import com.library.user_service.dto.UserRequestDTO;
import com.library.user_service.dto.UserResponseDTO;
import com.library.user_service.dto.UserUpdateDTO;
import com.library.user_service.model.Role;
import com.library.user_service.service.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void createUser_shouldReturnCreated() {

        UserRequestDTO requestDTO =
                UserRequestDTO.builder()
                        .name("Usuario Nuevo")
                        .email("nuevo@biblio.cl")
                        .role("USER")
                        .build();

        UserResponseDTO serviceResponse =
                buildUserResponse(
                        3L,
                        "Usuario Nuevo",
                        "nuevo@biblio.cl",
                        Role.USER,
                        true
                );

        when(userService.createUser(requestDTO))
                .thenReturn(serviceResponse);

        ResponseEntity<UserResponseDTO> response =
                userController.createUser(requestDTO);

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "nuevo@biblio.cl",
                response.getBody().getEmail()
        );

        verify(userService).createUser(requestDTO);
    }

    @Test
    void getAllUsers_shouldReturnOkAndUsers() {

        List<UserResponseDTO> users =
                List.of(
                        buildUserResponse(
                                1L,
                                "Primer Usuario",
                                "primero@biblio.cl",
                                Role.USER,
                                true
                        ),
                        buildUserResponse(
                                2L,
                                "Bibliotecario",
                                "bibliotecario@biblio.cl",
                                Role.BIBLIOTECARIO,
                                true
                        )
                );

        when(userService.getAllUsers())
                .thenReturn(users);

        ResponseEntity<List<UserResponseDTO>> response =
                userController.getAllUsers();

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                2,
                response.getBody().size()
        );

        verify(userService).getAllUsers();
    }

    @Test
    void getUserById_shouldReturnOk() {

        Long userId = 3L;

        UserResponseDTO serviceResponse =
                buildUserResponse(
                        userId,
                        "Usuario Prueba",
                        "usuario@biblio.cl",
                        Role.USER,
                        true
                );

        when(userService.getUserById(userId))
                .thenReturn(serviceResponse);

        ResponseEntity<UserResponseDTO> response =
                userController.getUserById(userId);

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                userId,
                response.getBody().getId()
        );

        verify(userService).getUserById(userId);
    }

    @Test
    void getUserByEmail_shouldReturnOk() {

        String email = "usuario@biblio.cl";

        UserResponseDTO serviceResponse =
                buildUserResponse(
                        3L,
                        "Usuario Prueba",
                        email,
                        Role.USER,
                        true
                );

        when(userService.getUserByEmail(email))
                .thenReturn(serviceResponse);

        ResponseEntity<UserResponseDTO> response =
                userController.getUserByEmail(email);

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                email,
                response.getBody().getEmail()
        );

        verify(userService).getUserByEmail(email);
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() {

        Long userId = 3L;

        UserUpdateDTO requestDTO =
                UserUpdateDTO.builder()
                        .name("Usuario Actualizado")
                        .email("usuario@biblio.cl")
                        .role("BIBLIOTECARIO")
                        .build();

        UserResponseDTO serviceResponse =
                buildUserResponse(
                        userId,
                        "Usuario Actualizado",
                        "usuario@biblio.cl",
                        Role.BIBLIOTECARIO,
                        true
                );

        when(
                userService.updateUser(
                        userId,
                        requestDTO
                )
        ).thenReturn(serviceResponse);

        ResponseEntity<UserResponseDTO> response =
                userController.updateUser(
                        userId,
                        requestDTO
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                Role.BIBLIOTECARIO,
                response.getBody().getRole()
        );

        verify(userService).updateUser(
                userId,
                requestDTO
        );
    }

    @Test
    void updateEmailInternally_shouldReturnUpdatedUser() {

        Long userId = 3L;

        InternalEmailUpdateDTO requestDTO =
                new InternalEmailUpdateDTO();

        requestDTO.setNewEmail(
                "correo.nuevo@biblio.cl"
        );

        UserResponseDTO serviceResponse =
                buildUserResponse(
                        userId,
                        "Usuario Prueba",
                        "correo.nuevo@biblio.cl",
                        Role.USER,
                        true
                );

        when(
                userService.updateEmailInternally(
                        userId,
                        "correo.nuevo@biblio.cl"
                )
        ).thenReturn(serviceResponse);

        ResponseEntity<UserResponseDTO> response =
                userController.updateEmailInternally(
                        userId,
                        requestDTO
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "correo.nuevo@biblio.cl",
                response.getBody().getEmail()
        );

        verify(userService).updateEmailInternally(
                userId,
                "correo.nuevo@biblio.cl"
        );
    }

    @Test
    void deleteUser_shouldReturnNoContent() {

        Long userId = 3L;

        ResponseEntity<Void> response =
                userController.deleteUser(userId);

        assertEquals(
                HttpStatus.NO_CONTENT,
                response.getStatusCode()
        );

        verify(userService).deleteUser(userId);
    }

    private UserResponseDTO buildUserResponse(
            Long id,
            String name,
            String email,
            Role role,
            boolean active
    ) {

        return UserResponseDTO.builder()
                .id(id)
                .name(name)
                .email(email)
                .role(role)
                .active(active)
                .build();
    }
}