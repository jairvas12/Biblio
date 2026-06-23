package com.library.user_service.service.impl;

import com.library.user_service.dto.UserRequestDTO;
import com.library.user_service.dto.UserResponseDTO;
import com.library.user_service.dto.UserUpdateDTO;
import com.library.user_service.exception.EmailAlreadyExistsException;
import com.library.user_service.exception.EmailChangeNotAllowedException;
import com.library.user_service.exception.UserNotFoundException;
import com.library.user_service.model.Role;
import com.library.user_service.model.User;
import com.library.user_service.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUserById_shouldReturnUser_whenUserExists() {

        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .name("Usuario Prueba")
                .email("usuario@biblio.cl")
                .role(Role.USER)
                .active(true)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        UserResponseDTO response =
                userService.getUserById(userId);

        assertEquals(userId, response.getId());
        assertEquals("Usuario Prueba", response.getName());
        assertEquals("usuario@biblio.cl", response.getEmail());
        assertEquals(Role.USER, response.getRole());
        assertTrue(response.getActive());

        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_shouldThrowException_whenUserDoesNotExist() {

        Long userId = 9999L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        UserNotFoundException exception =
                assertThrows(
                        UserNotFoundException.class,
                        () -> userService.getUserById(userId)
                );

        assertTrue(
                exception.getMessage()
                        .contains(userId.toString())
        );

        verify(userRepository).findById(userId);
    }

    @Test
    void createUser_shouldCreateUser_whenEmailIsAvailable() {

        UserRequestDTO requestDTO =
                UserRequestDTO.builder()
                        .name("  Nuevo Usuario  ")
                        .email("NUEVO@BIBLIO.CL")
                        .role("USER")
                        .build();

        when(
                userRepository.existsByEmailIgnoreCase(
                        "nuevo@biblio.cl"
                )
        ).thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {

                    User user =
                            invocation.getArgument(0);

                    user.setId(3L);

                    return user;
                });

        UserResponseDTO response =
                userService.createUser(requestDTO);

        assertEquals(3L, response.getId());
        assertEquals("Nuevo Usuario", response.getName());
        assertEquals(
                "nuevo@biblio.cl",
                response.getEmail()
        );
        assertEquals(Role.USER, response.getRole());
        assertTrue(response.getActive());

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(
                userCaptor.capture()
        );

        User savedUser =
                userCaptor.getValue();

        assertEquals(
                "Nuevo Usuario",
                savedUser.getName()
        );

        assertEquals(
                "nuevo@biblio.cl",
                savedUser.getEmail()
        );

        assertEquals(
                Role.USER,
                savedUser.getRole()
        );

        assertTrue(savedUser.getActive());
    }

    @Test
    void createUser_shouldThrowException_whenEmailAlreadyExists() {

        UserRequestDTO requestDTO =
                UserRequestDTO.builder()
                        .name("Usuario Duplicado")
                        .email("duplicado@biblio.cl")
                        .role("USER")
                        .build();

        when(
                userRepository.existsByEmailIgnoreCase(
                        "duplicado@biblio.cl"
                )
        ).thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.createUser(requestDTO)
        );

        verify(
                userRepository,
                never()
        ).save(any(User.class));
    }

    @Test
    void updateUser_shouldUpdateNameAndRole_whenEmailDoesNotChange() {

        Long userId = 3L;

        User existingUser = User.builder()
                .id(userId)
                .name("Nombre Anterior")
                .email("usuario@biblio.cl")
                .role(Role.USER)
                .active(true)
                .build();

        UserUpdateDTO requestDTO =
                UserUpdateDTO.builder()
                        .name("  Nombre Actualizado  ")
                        .email("USUARIO@BIBLIO.CL")
                        .role("BIBLIOTECARIO")
                        .build();

        when(userRepository.findById(userId))
                .thenReturn(
                        Optional.of(existingUser)
                );

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        UserResponseDTO response =
                userService.updateUser(
                        userId,
                        requestDTO
                );

        assertEquals(
                "Nombre Actualizado",
                response.getName()
        );

        assertEquals(
                "usuario@biblio.cl",
                response.getEmail()
        );

        assertEquals(
                Role.BIBLIOTECARIO,
                response.getRole()
        );

        assertTrue(response.getActive());

        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUser_shouldRejectDirectEmailChange() {

        Long userId = 3L;

        User existingUser = User.builder()
                .id(userId)
                .name("Usuario Prueba")
                .email("correo.actual@biblio.cl")
                .role(Role.USER)
                .active(true)
                .build();

        UserUpdateDTO requestDTO =
                UserUpdateDTO.builder()
                        .name("Usuario Prueba")
                        .email("correo.nuevo@biblio.cl")
                        .role("USER")
                        .build();
                requestDTO.setRole("USER");

        when(userRepository.findById(userId))
                .thenReturn(
                        Optional.of(existingUser)
                );

        EmailChangeNotAllowedException exception =
                assertThrows(
                        EmailChangeNotAllowedException.class,
                        () -> userService.updateUser(
                                userId,
                                requestDTO
                        )
                );

        assertTrue(
                exception.getMessage()
                        .toLowerCase()
                        .contains("correo")
        );

        verify(
                userRepository,
                never()
        ).save(any(User.class));
    }
    @Test
    void getAllUsers_shouldReturnUsers() {

        User firstUser = User.builder()
                .id(1L)
                .name("Primer Usuario")
                .email("primero@biblio.cl")
                .role(Role.USER)
                .active(true)
                .build();

        User secondUser = User.builder()
                .id(2L)
                .name("Segundo Usuario")
                .email("segundo@biblio.cl")
                .role(Role.BIBLIOTECARIO)
                .active(true)
                .build();

        when(userRepository.findAll())
                .thenReturn(
                        List.of(
                                firstUser,
                                secondUser
                        )
                );

        List<UserResponseDTO> response =
                userService.getAllUsers();

        assertEquals(2, response.size());

        assertEquals(
                "Primer Usuario",
                response.get(0).getName()
        );

        assertEquals(
                Role.BIBLIOTECARIO,
                response.get(1).getRole()
        );

        verify(userRepository).findAll();
    }

    @Test
    void getUserByEmail_shouldReturnUser_whenEmailExists() {

        String email = "usuario@biblio.cl";

        User user = User.builder()
                .id(3L)
                .name("Usuario Correo")
                .email(email)
                .role(Role.USER)
                .active(true)
                .build();

        when(
                userRepository.findByEmailIgnoreCase(email)
        ).thenReturn(Optional.of(user));

        UserResponseDTO response =
                userService.getUserByEmail(email);

        assertEquals(3L, response.getId());
        assertEquals(email, response.getEmail());
        assertEquals(Role.USER, response.getRole());

        verify(
                userRepository
        ).findByEmailIgnoreCase(email);
    }

    @Test
    void updateEmailInternally_shouldUpdateEmail_whenEmailIsAvailable() {

        Long userId = 3L;

        User existingUser = User.builder()
                .id(userId)
                .name("Usuario Prueba")
                .email("correo.anterior@biblio.cl")
                .role(Role.USER)
                .active(true)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(
                        Optional.of(existingUser)
                );

        when(
                userRepository
                        .existsByEmailIgnoreCaseAndIdNot(
                                "correo.nuevo@biblio.cl",
                                userId
                        )
        ).thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        UserResponseDTO response =
                userService.updateEmailInternally(
                        userId,
                        "CORREO.NUEVO@BIBLIO.CL"
                );

        assertEquals(
                "correo.nuevo@biblio.cl",
                response.getEmail()
        );

        assertEquals(
                "correo.nuevo@biblio.cl",
                existingUser.getEmail()
        );

        verify(userRepository).save(existingUser);
    }

    @Test
    void updateEmailInternally_shouldNotSave_whenEmailIsTheSame() {

        Long userId = 3L;

        User existingUser = User.builder()
                .id(userId)
                .name("Usuario Prueba")
                .email("usuario@biblio.cl")
                .role(Role.USER)
                .active(true)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(
                        Optional.of(existingUser)
                );

        UserResponseDTO response =
                userService.updateEmailInternally(
                        userId,
                        "USUARIO@BIBLIO.CL"
                );

        assertEquals(
                "usuario@biblio.cl",
                response.getEmail()
        );

        verify(
                userRepository,
                never()
        ).save(any(User.class));
    }

    @Test
    void updateEmailInternally_shouldRejectDuplicatedEmail() {

        Long userId = 3L;

        User existingUser = User.builder()
                .id(userId)
                .name("Usuario Prueba")
                .email("correo.actual@biblio.cl")
                .role(Role.USER)
                .active(true)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(
                        Optional.of(existingUser)
                );

        when(
                userRepository
                        .existsByEmailIgnoreCaseAndIdNot(
                                "ocupado@biblio.cl",
                                userId
                        )
        ).thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.updateEmailInternally(
                        userId,
                        "ocupado@biblio.cl"
                )
        );

        verify(
                userRepository,
                never()
        ).save(any(User.class));
    }

    @Test
    void deleteUser_shouldDeactivateUser() {

        Long userId = 3L;

        User existingUser = User.builder()
                .id(userId)
                .name("Usuario Activo")
                .email("activo@biblio.cl")
                .role(Role.USER)
                .active(true)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(
                        Optional.of(existingUser)
                );

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        userService.deleteUser(userId);

        assertFalse(existingUser.getActive());

        verify(userRepository).save(existingUser);
    }
    @Test
    void getUserByEmail_shouldThrowException_whenEmailDoesNotExist() {

        String email = "inexistente@biblio.cl";

        when(
                userRepository.findByEmailIgnoreCase(email)
        ).thenReturn(Optional.empty());

        UserNotFoundException exception =
                assertThrows(
                        UserNotFoundException.class,
                        () -> userService.getUserByEmail(email)
                );

        assertTrue(
                exception.getMessage()
                        .contains(email)
        );

        verify(
                userRepository
        ).findByEmailIgnoreCase(email);
    }

    @Test
    void createUser_shouldThrowException_whenRoleIsInvalid() {

        UserRequestDTO requestDTO =
                UserRequestDTO.builder()
                        .name("Usuario Rol Inválido")
                        .email("rol.invalido@biblio.cl")
                        .role("SUPERADMIN")
                        .build();

        when(
                userRepository.existsByEmailIgnoreCase(
                        "rol.invalido@biblio.cl"
                )
        ).thenReturn(false);

        assertThrows(
                RuntimeException.class,
                () -> userService.createUser(requestDTO)
        );

        verify(
                userRepository,
                never()
        ).save(any(User.class));
    }

    @Test
    void deleteUser_shouldNotSave_whenUserIsAlreadyInactive() {

        Long userId = 5L;

        User inactiveUser = User.builder()
                .id(userId)
                .name("Usuario Inactivo")
                .email("inactivo@biblio.cl")
                .role(Role.USER)
                .active(false)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(inactiveUser));

        userService.deleteUser(userId);

        assertFalse(inactiveUser.getActive());

        verify(
                userRepository,
                never()
        ).save(any(User.class));
    }
}