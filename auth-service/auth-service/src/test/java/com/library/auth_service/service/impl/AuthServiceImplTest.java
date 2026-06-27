package com.library.auth_service.service.impl;

import com.library.auth_service.client.UserClient;
import com.library.auth_service.dto.AuthResponseDTO;
import com.library.auth_service.dto.LoginRequestDTO;
import com.library.auth_service.dto.RegisterRequestDTO;
import com.library.auth_service.dto.UserRequestDTO;
import com.library.auth_service.dto.UserResponseDTO;
import com.library.auth_service.exception.EmailAlreadyRegisteredException;
import com.library.auth_service.exception.InvalidCredentialsException;
import com.library.auth_service.model.AuthCredential;
import com.library.auth_service.repository.AuthCredentialRepository;
import com.library.auth_service.security.JwtService;
import com.library.auth_service.dto.ChangeEmailRequestDTO;
import com.library.auth_service.dto.InternalEmailUpdateDTO;
import com.library.auth_service.exception.CredentialNotFoundException;

import com.library.auth_service.exception.RegistrationProcessException;
import com.library.auth_service.exception.UserInactiveException;
import com.library.auth_service.exception.UserServiceUnavailableException;

import com.library.auth_service.exception.EmailChangeProcessException;

import java.util.List;
import feign.FeignException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;


import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserClient userClient;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthCredentialRepository credentialRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_shouldCreateCredentialAndReturnToken() {

        RegisterRequestDTO requestDTO =
                new RegisterRequestDTO();

        requestDTO.setName(
                "  Usuario   Nuevo  "
        );

        requestDTO.setEmail(
                " NUEVO@BIBLIO.CL "
        );

        requestDTO.setPassword(
                "ClaveSegura123"
        );

        UserResponseDTO createdUser =
                UserResponseDTO.builder()
                        .id(3L)
                        .name("Usuario Nuevo")
                        .email("nuevo@biblio.cl")
                        .role("USER")
                        .active(true)
                        .build();

        when(
                credentialRepository.existsByEmailIgnoreCase(
                        "nuevo@biblio.cl"
                )
        ).thenReturn(false);

        when(
                userClient.createUser(
                        any(UserRequestDTO.class)
                )
        ).thenReturn(createdUser);

        when(
                passwordEncoder.encode(
                        "ClaveSegura123"
                )
        ).thenReturn("HASH_BCRYPT");

        when(
                credentialRepository.save(
                        any(AuthCredential.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        when(
                jwtService.generateToken(
                        3L,
                        "nuevo@biblio.cl",
                        "USER"
                )
        ).thenReturn("jwt-registro");

        AuthResponseDTO response =
                authService.register(requestDTO);

        assertEquals(
                "jwt-registro",
                response.getToken()
        );

        assertEquals(
                "Bearer",
                response.getTokenType()
        );

        assertEquals(
                3L,
                response.getUserId()
        );

        assertEquals(
                "nuevo@biblio.cl",
                response.getEmail()
        );

        assertEquals(
                "USER",
                response.getRole()
        );

        ArgumentCaptor<UserRequestDTO> userRequestCaptor =
                ArgumentCaptor.forClass(
                        UserRequestDTO.class
                );

        verify(userClient).createUser(
                userRequestCaptor.capture()
        );

        UserRequestDTO sentUserRequest =
                userRequestCaptor.getValue();

        assertEquals(
                "Usuario Nuevo",
                sentUserRequest.getName()
        );

        assertEquals(
                "nuevo@biblio.cl",
                sentUserRequest.getEmail()
        );

        assertEquals(
                "USER",
                sentUserRequest.getRole()
        );

        ArgumentCaptor<AuthCredential> credentialCaptor =
                ArgumentCaptor.forClass(
                        AuthCredential.class
                );

        verify(credentialRepository).save(
                credentialCaptor.capture()
        );

        AuthCredential savedCredential =
                credentialCaptor.getValue();

        assertEquals(
                3L,
                savedCredential.getUserId()
        );

        assertEquals(
                "nuevo@biblio.cl",
                savedCredential.getEmail()
        );

        assertEquals(
                "HASH_BCRYPT",
                savedCredential.getPasswordHash()
        );

        verify(jwtService).generateToken(
                3L,
                "nuevo@biblio.cl",
                "USER"
        );
    }

    @Test
    void register_shouldRejectEmailAlreadyRegistered() {

        RegisterRequestDTO requestDTO =
                new RegisterRequestDTO();

        requestDTO.setName(
                "Usuario Duplicado"
        );

        requestDTO.setEmail(
                "duplicado@biblio.cl"
        );

        requestDTO.setPassword(
                "ClaveSegura123"
        );

        when(
                credentialRepository.existsByEmailIgnoreCase(
                        "duplicado@biblio.cl"
                )
        ).thenReturn(true);

        assertThrows(
                EmailAlreadyRegisteredException.class,
                () -> authService.register(requestDTO)
        );

        verify(
                credentialRepository,
                never()
        ).save(any(AuthCredential.class));

        verifyNoInteractions(
                userClient,
                passwordEncoder,
                jwtService
        );
    }

    @Test
    void login_shouldReturnToken_whenCredentialsAreCorrect() {

        LoginRequestDTO requestDTO =
                new LoginRequestDTO();

        requestDTO.setEmail(
                " NUEVO@BIBLIO.CL "
        );

        requestDTO.setPassword(
                "ClaveSegura123"
        );

        AuthCredential credential =
                AuthCredential.builder()
                        .id(10L)
                        .userId(3L)
                        .email("nuevo@biblio.cl")
                        .passwordHash("HASH_BCRYPT")
                        .build();

        UserResponseDTO activeUser =
                UserResponseDTO.builder()
                        .id(3L)
                        .name("Usuario Nuevo")
                        .email("nuevo@biblio.cl")
                        .role("USER")
                        .active(true)
                        .build();

        when(
                credentialRepository.findByEmailIgnoreCase(
                        "nuevo@biblio.cl"
                )
        ).thenReturn(Optional.of(credential));

        when(
                passwordEncoder.matches(
                        "ClaveSegura123",
                        "HASH_BCRYPT"
                )
        ).thenReturn(true);

        when(
                userClient.getUserByEmail(
                        "nuevo@biblio.cl"
                )
        ).thenReturn(activeUser);

        when(
                jwtService.generateToken(
                        3L,
                        "nuevo@biblio.cl",
                        "USER"
                )
        ).thenReturn("jwt-login");

        AuthResponseDTO response =
                authService.login(requestDTO);

        assertEquals(
                "jwt-login",
                response.getToken()
        );

        assertEquals(
                "Bearer",
                response.getTokenType()
        );

        assertEquals(
                3L,
                response.getUserId()
        );

        assertEquals(
                "nuevo@biblio.cl",
                response.getEmail()
        );

        assertEquals(
                "USER",
                response.getRole()
        );

        verify(userClient).getUserByEmail(
                "nuevo@biblio.cl"
        );

        verify(jwtService).generateToken(
                3L,
                "nuevo@biblio.cl",
                "USER"
        );
    }

    @Test
    void login_shouldRejectIncorrectPassword() {

        LoginRequestDTO requestDTO =
                new LoginRequestDTO();

        requestDTO.setEmail(
                "usuario@biblio.cl"
        );

        requestDTO.setPassword(
                "ClaveIncorrecta"
        );

        AuthCredential credential =
                AuthCredential.builder()
                        .id(10L)
                        .userId(3L)
                        .email("usuario@biblio.cl")
                        .passwordHash("HASH_BCRYPT")
                        .build();

        when(
                credentialRepository.findByEmailIgnoreCase(
                        "usuario@biblio.cl"
                )
        ).thenReturn(Optional.of(credential));

        when(
                passwordEncoder.matches(
                        "ClaveIncorrecta",
                        "HASH_BCRYPT"
                )
        ).thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(requestDTO)
        );

        verify(
                userClient,
                never()
        ).getUserByEmail(any());

        verifyNoInteractions(jwtService);
    }
    @Test
    void register_shouldRejectWhenUserServiceReportsConflict() {

        RegisterRequestDTO requestDTO =
                new RegisterRequestDTO();

        requestDTO.setName(
                "Usuario Conflicto"
        );

        requestDTO.setEmail(
                "conflicto@biblio.cl"
        );

        requestDTO.setPassword(
                "ClaveSegura123"
        );

        when(
                credentialRepository.existsByEmailIgnoreCase(
                        "conflicto@biblio.cl"
                )
        ).thenReturn(false);

        FeignException feignException =
                mock(FeignException.class);

        when(feignException.status())
                .thenReturn(409);

        when(
                userClient.createUser(
                        any(UserRequestDTO.class)
                )
        ).thenThrow(feignException);

        assertThrows(
                EmailAlreadyRegisteredException.class,
                () -> authService.register(requestDTO)
        );

        verify(
                credentialRepository,
                never()
        ).save(any(AuthCredential.class));

        verifyNoInteractions(
                passwordEncoder,
                jwtService
        );
    }

    @Test
    void register_shouldReturnUnavailable_whenUserServiceFails() {

        RegisterRequestDTO requestDTO =
                new RegisterRequestDTO();

        requestDTO.setName(
                "Usuario Remoto"
        );

        requestDTO.setEmail(
                "remoto@biblio.cl"
        );

        requestDTO.setPassword(
                "ClaveSegura123"
        );

        when(
                credentialRepository.existsByEmailIgnoreCase(
                        "remoto@biblio.cl"
                )
        ).thenReturn(false);

        FeignException feignException =
                mock(FeignException.class);

        when(feignException.status())
                .thenReturn(503);

        when(
                userClient.createUser(
                        any(UserRequestDTO.class)
                )
        ).thenThrow(feignException);

        assertThrows(
                UserServiceUnavailableException.class,
                () -> authService.register(requestDTO)
        );

        verify(
                credentialRepository,
                never()
        ).save(any(AuthCredential.class));

        verifyNoInteractions(
                passwordEncoder,
                jwtService
        );
    }

    @Test
    void login_shouldRejectEmailWithoutCredential() {

        LoginRequestDTO requestDTO =
                new LoginRequestDTO();

        requestDTO.setEmail(
                "inexistente@biblio.cl"
        );

        requestDTO.setPassword(
                "ClaveSegura123"
        );

        when(
                credentialRepository.findByEmailIgnoreCase(
                        "inexistente@biblio.cl"
                )
        ).thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(requestDTO)
        );

        verifyNoInteractions(
                passwordEncoder,
                userClient,
                jwtService
        );
    }

    @Test
    void login_shouldRejectInactiveUser() {

        LoginRequestDTO requestDTO =
                new LoginRequestDTO();

        requestDTO.setEmail(
                "inactivo@biblio.cl"
        );

        requestDTO.setPassword(
                "ClaveSegura123"
        );

        AuthCredential credential =
                AuthCredential.builder()
                        .id(20L)
                        .userId(5L)
                        .email("inactivo@biblio.cl")
                        .passwordHash("HASH_BCRYPT")
                        .build();

        UserResponseDTO inactiveUser =
                UserResponseDTO.builder()
                        .id(5L)
                        .name("Usuario Inactivo")
                        .email("inactivo@biblio.cl")
                        .role("USER")
                        .active(false)
                        .build();

        when(
                credentialRepository.findByEmailIgnoreCase(
                        "inactivo@biblio.cl"
                )
        ).thenReturn(Optional.of(credential));

        when(
                passwordEncoder.matches(
                        "ClaveSegura123",
                        "HASH_BCRYPT"
                )
        ).thenReturn(true);

        when(
                userClient.getUserByEmail(
                        "inactivo@biblio.cl"
                )
        ).thenReturn(inactiveUser);

        assertThrows(
                UserInactiveException.class,
                () -> authService.login(requestDTO)
        );

        verify(
                jwtService,
                never()
        ).generateToken(
                any(),
                any(),
                any()
        );
    }
    @Test
    void register_shouldCompensateUser_whenCredentialSaveFails() {

        RegisterRequestDTO requestDTO =
                new RegisterRequestDTO();

        requestDTO.setName(
                "Usuario Compensado"
        );

        requestDTO.setEmail(
                "compensado@biblio.cl"
        );

        requestDTO.setPassword(
                "ClaveSegura123"
        );

        UserResponseDTO createdUser =
                UserResponseDTO.builder()
                        .id(8L)
                        .name("Usuario Compensado")
                        .email("compensado@biblio.cl")
                        .role("USER")
                        .active(true)
                        .build();

        when(
                credentialRepository.existsByEmailIgnoreCase(
                        "compensado@biblio.cl"
                )
        ).thenReturn(false);

        when(
                userClient.createUser(
                        any(UserRequestDTO.class)
                )
        ).thenReturn(createdUser);

        when(
                passwordEncoder.encode(
                        "ClaveSegura123"
                )
        ).thenReturn("HASH_BCRYPT");

        when(
                credentialRepository.save(
                        any(AuthCredential.class)
                )
        ).thenThrow(
                new RuntimeException(
                        "Error de base de datos"
                )
        );

        assertThrows(
                RegistrationProcessException.class,
                () -> authService.register(requestDTO)
        );

        verify(userClient).deactivateUser(
                8L
        );

        verify(
                jwtService,
                never()
        ).generateToken(
                any(),
                any(),
                any()
        );
    }

    @Test
    void register_shouldThrowRegistrationException_evenWhenCompensationFails() {

        RegisterRequestDTO requestDTO =
                new RegisterRequestDTO();

        requestDTO.setName(
                "Usuario Sin Compensacion"
        );

        requestDTO.setEmail(
                "sincompensacion@biblio.cl"
        );

        requestDTO.setPassword(
                "ClaveSegura123"
        );

        UserResponseDTO createdUser =
                UserResponseDTO.builder()
                        .id(9L)
                        .name("Usuario Sin Compensacion")
                        .email("sincompensacion@biblio.cl")
                        .role("USER")
                        .active(true)
                        .build();

        when(
                credentialRepository.existsByEmailIgnoreCase(
                        "sincompensacion@biblio.cl"
                )
        ).thenReturn(false);

        when(
                userClient.createUser(
                        any(UserRequestDTO.class)
                )
        ).thenReturn(createdUser);

        when(
                passwordEncoder.encode(
                        "ClaveSegura123"
                )
        ).thenReturn("HASH_BCRYPT");

        when(
                credentialRepository.save(
                        any(AuthCredential.class)
                )
        ).thenThrow(
                new RuntimeException(
                        "Error guardando credencial"
                )
        );

        doThrow(
                new RuntimeException(
                        "Error compensando usuario"
                )
        ).when(userClient)
                .deactivateUser(9L);

        assertThrows(
                RegistrationProcessException.class,
                () -> authService.register(requestDTO)
        );

        verify(userClient).deactivateUser(
                9L
        );

        verify(
                jwtService,
                never()
        ).generateToken(
                any(),
                any(),
                any()
        );
    }

    @Test
    void login_shouldReject_whenUserDoesNotExistInUserService() {

        LoginRequestDTO requestDTO =
                new LoginRequestDTO();

        requestDTO.setEmail(
                "desincronizado@biblio.cl"
        );

        requestDTO.setPassword(
                "ClaveSegura123"
        );

        AuthCredential credential =
                AuthCredential.builder()
                        .id(30L)
                        .userId(15L)
                        .email("desincronizado@biblio.cl")
                        .passwordHash("HASH_BCRYPT")
                        .build();

        when(
                credentialRepository.findByEmailIgnoreCase(
                        "desincronizado@biblio.cl"
                )
        ).thenReturn(Optional.of(credential));

        when(
                passwordEncoder.matches(
                        "ClaveSegura123",
                        "HASH_BCRYPT"
                )
        ).thenReturn(true);

        FeignException feignException =
                mock(FeignException.class);

        when(feignException.status())
                .thenReturn(404);

        when(
                userClient.getUserByEmail(
                        "desincronizado@biblio.cl"
                )
        ).thenThrow(feignException);

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(requestDTO)
        );

        verify(
                jwtService,
                never()
        ).generateToken(
                any(),
                any(),
                any()
        );
    }

    @Test
    void login_shouldReturnUnavailable_whenUserServiceFails() {

        LoginRequestDTO requestDTO =
                new LoginRequestDTO();

        requestDTO.setEmail(
                "usuario@biblio.cl"
        );

        requestDTO.setPassword(
                "ClaveSegura123"
        );

        AuthCredential credential =
                AuthCredential.builder()
                        .id(31L)
                        .userId(16L)
                        .email("usuario@biblio.cl")
                        .passwordHash("HASH_BCRYPT")
                        .build();

        when(
                credentialRepository.findByEmailIgnoreCase(
                        "usuario@biblio.cl"
                )
        ).thenReturn(Optional.of(credential));

        when(
                passwordEncoder.matches(
                        "ClaveSegura123",
                        "HASH_BCRYPT"
                )
        ).thenReturn(true);

        FeignException feignException =
                mock(FeignException.class);

        when(feignException.status())
                .thenReturn(503);

        when(
                userClient.getUserByEmail(
                        "usuario@biblio.cl"
                )
        ).thenThrow(feignException);

        assertThrows(
                UserServiceUnavailableException.class,
                () -> authService.login(requestDTO)
        );

        verify(
                jwtService,
                never()
        ).generateToken(
                any(),
                any(),
                any()
        );
    }
    @Test
    void changeUserEmail_shouldRejectWhenCredentialDoesNotExist() {

        ChangeEmailRequestDTO requestDTO =
                new ChangeEmailRequestDTO();

        requestDTO.setNewEmail(
                "nuevo@biblio.cl"
        );

        when(
                credentialRepository.findByUserId(50L)
        ).thenReturn(Optional.empty());

        assertThrows(
                CredentialNotFoundException.class,
                () -> authService.changeUserEmail(
                        50L,
                        requestDTO
                )
        );

        verifyNoInteractions(userClient);
    }

    @Test
    void changeUserEmail_shouldReturnCurrentUser_whenEmailIsUnchanged() {

        ChangeEmailRequestDTO requestDTO =
                new ChangeEmailRequestDTO();

        requestDTO.setNewEmail(
                " USUARIO@BIBLIO.CL "
        );

        AuthCredential credential =
                AuthCredential.builder()
                        .id(40L)
                        .userId(20L)
                        .email("usuario@biblio.cl")
                        .passwordHash("HASH_BCRYPT")
                        .build();

        UserResponseDTO currentUser =
                UserResponseDTO.builder()
                        .id(20L)
                        .name("Usuario Actual")
                        .email("usuario@biblio.cl")
                        .role("USER")
                        .active(true)
                        .build();

        when(
                credentialRepository.findByUserId(20L)
        ).thenReturn(Optional.of(credential));

        when(
                userClient.getUserByEmail(
                        "usuario@biblio.cl"
                )
        ).thenReturn(currentUser);

        UserResponseDTO response =
                authService.changeUserEmail(
                        20L,
                        requestDTO
                );

        assertEquals(
                "usuario@biblio.cl",
                response.getEmail()
        );

        verify(userClient).getUserByEmail(
                "usuario@biblio.cl"
        );

        verify(
                credentialRepository,
                never()
        ).saveAndFlush(any(AuthCredential.class));
    }

    @Test
    void changeUserEmail_shouldRejectEmailUsedByAnotherCredential() {

        ChangeEmailRequestDTO requestDTO =
                new ChangeEmailRequestDTO();

        requestDTO.setNewEmail(
                "ocupado@biblio.cl"
        );

        AuthCredential credential =
                AuthCredential.builder()
                        .id(41L)
                        .userId(21L)
                        .email("actual@biblio.cl")
                        .passwordHash("HASH_BCRYPT")
                        .build();

        when(
                credentialRepository.findByUserId(21L)
        ).thenReturn(Optional.of(credential));

        when(
                credentialRepository
                        .existsByEmailIgnoreCaseAndUserIdNot(
                                "ocupado@biblio.cl",
                                21L
                        )
        ).thenReturn(true);

        assertThrows(
                EmailAlreadyRegisteredException.class,
                () -> authService.changeUserEmail(
                        21L,
                        requestDTO
                )
        );

        verify(
                userClient,
                never()
        ).updateEmailInternally(
                any(),
                any(InternalEmailUpdateDTO.class)
        );

        verify(
                credentialRepository,
                never()
        ).saveAndFlush(any(AuthCredential.class));
    }

    @Test
    void changeUserEmail_shouldUpdateUserAndCredentialSuccessfully() {

        ChangeEmailRequestDTO requestDTO =
                new ChangeEmailRequestDTO();

        requestDTO.setNewEmail(
                " NUEVO@BIBLIO.CL "
        );

        AuthCredential credential =
                AuthCredential.builder()
                        .id(42L)
                        .userId(22L)
                        .email("anterior@biblio.cl")
                        .passwordHash("HASH_BCRYPT")
                        .build();

        UserResponseDTO updatedUser =
                UserResponseDTO.builder()
                        .id(22L)
                        .name("Usuario Actualizado")
                        .email("nuevo@biblio.cl")
                        .role("USER")
                        .active(true)
                        .build();

        when(
                credentialRepository.findByUserId(22L)
        ).thenReturn(Optional.of(credential));

        when(
                credentialRepository
                        .existsByEmailIgnoreCaseAndUserIdNot(
                                "nuevo@biblio.cl",
                                22L
                        )
        ).thenReturn(false);

        when(
                userClient.updateEmailInternally(
                        any(),
                        any(InternalEmailUpdateDTO.class)
                )
        ).thenReturn(updatedUser);

        when(
                credentialRepository.saveAndFlush(
                        any(AuthCredential.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        UserResponseDTO response =
                authService.changeUserEmail(
                        22L,
                        requestDTO
                );

        assertEquals(
                "nuevo@biblio.cl",
                response.getEmail()
        );

        ArgumentCaptor<InternalEmailUpdateDTO> internalCaptor =
                ArgumentCaptor.forClass(
                        InternalEmailUpdateDTO.class
                );

        verify(userClient).updateEmailInternally(
                org.mockito.ArgumentMatchers.eq(22L),
                internalCaptor.capture()
        );

        assertEquals(
                "nuevo@biblio.cl",
                internalCaptor.getValue().getNewEmail()
        );

        ArgumentCaptor<AuthCredential> credentialCaptor =
                ArgumentCaptor.forClass(
                        AuthCredential.class
                );

        verify(credentialRepository).saveAndFlush(
                credentialCaptor.capture()
        );

        assertEquals(
                "nuevo@biblio.cl",
                credentialCaptor.getValue().getEmail()
        );
    }
    @Test
    void changeUserEmail_shouldRejectConflictReportedByUserService() {

        ChangeEmailRequestDTO requestDTO =
                new ChangeEmailRequestDTO();

        requestDTO.setNewEmail(
                "ocupado@biblio.cl"
        );

        AuthCredential credential =
                AuthCredential.builder()
                        .id(50L)
                        .userId(30L)
                        .email("actual@biblio.cl")
                        .passwordHash("HASH_BCRYPT")
                        .build();

        when(
                credentialRepository.findByUserId(30L)
        ).thenReturn(Optional.of(credential));

        when(
                credentialRepository
                        .existsByEmailIgnoreCaseAndUserIdNot(
                                "ocupado@biblio.cl",
                                30L
                        )
        ).thenReturn(false);

        FeignException.Conflict conflictException =
                mock(FeignException.Conflict.class);

        when(
                userClient.updateEmailInternally(
                        any(),
                        any(InternalEmailUpdateDTO.class)
                )
        ).thenThrow(conflictException);

        assertThrows(
                EmailAlreadyRegisteredException.class,
                () -> authService.changeUserEmail(
                        30L,
                        requestDTO
                )
        );

        verify(
                credentialRepository,
                never()
        ).saveAndFlush(any(AuthCredential.class));
    }

    @Test
    void changeUserEmail_shouldRejectWhenUserDoesNotExistInUserService() {

        ChangeEmailRequestDTO requestDTO =
                new ChangeEmailRequestDTO();

        requestDTO.setNewEmail(
                "nuevo@biblio.cl"
        );

        AuthCredential credential =
                AuthCredential.builder()
                        .id(51L)
                        .userId(31L)
                        .email("actual@biblio.cl")
                        .passwordHash("HASH_BCRYPT")
                        .build();

        when(
                credentialRepository.findByUserId(31L)
        ).thenReturn(Optional.of(credential));

        when(
                credentialRepository
                        .existsByEmailIgnoreCaseAndUserIdNot(
                                "nuevo@biblio.cl",
                                31L
                        )
        ).thenReturn(false);

        FeignException.NotFound notFoundException =
                mock(FeignException.NotFound.class);

        when(
                userClient.updateEmailInternally(
                        any(),
                        any(InternalEmailUpdateDTO.class)
                )
        ).thenThrow(notFoundException);

        assertThrows(
                CredentialNotFoundException.class,
                () -> authService.changeUserEmail(
                        31L,
                        requestDTO
                )
        );

        verify(
                credentialRepository,
                never()
        ).saveAndFlush(any(AuthCredential.class));
    }

    @Test
    void changeUserEmail_shouldReturnUnavailable_whenUserServiceFails() {

        ChangeEmailRequestDTO requestDTO =
                new ChangeEmailRequestDTO();

        requestDTO.setNewEmail(
                "nuevo@biblio.cl"
        );

        AuthCredential credential =
                AuthCredential.builder()
                        .id(52L)
                        .userId(32L)
                        .email("actual@biblio.cl")
                        .passwordHash("HASH_BCRYPT")
                        .build();

        when(
                credentialRepository.findByUserId(32L)
        ).thenReturn(Optional.of(credential));

        when(
                credentialRepository
                        .existsByEmailIgnoreCaseAndUserIdNot(
                                "nuevo@biblio.cl",
                                32L
                        )
        ).thenReturn(false);

        FeignException feignException =
                mock(FeignException.class);

        when(feignException.status())
                .thenReturn(503);

        when(
                userClient.updateEmailInternally(
                        any(),
                        any(InternalEmailUpdateDTO.class)
                )
        ).thenThrow(feignException);

        assertThrows(
                UserServiceUnavailableException.class,
                () -> authService.changeUserEmail(
                        32L,
                        requestDTO
                )
        );

        verify(
                credentialRepository,
                never()
        ).saveAndFlush(any(AuthCredential.class));
    }

    @Test
    void changeUserEmail_shouldRestorePreviousEmail_whenAuthDatabaseFails() {

        ChangeEmailRequestDTO requestDTO =
                new ChangeEmailRequestDTO();

        requestDTO.setNewEmail(
                "nuevo@biblio.cl"
        );

        AuthCredential credential =
                AuthCredential.builder()
                        .id(53L)
                        .userId(33L)
                        .email("anterior@biblio.cl")
                        .passwordHash("HASH_BCRYPT")
                        .build();

        UserResponseDTO updatedUser =
                UserResponseDTO.builder()
                        .id(33L)
                        .name("Usuario")
                        .email("nuevo@biblio.cl")
                        .role("USER")
                        .active(true)
                        .build();

        UserResponseDTO restoredUser =
                UserResponseDTO.builder()
                        .id(33L)
                        .name("Usuario")
                        .email("anterior@biblio.cl")
                        .role("USER")
                        .active(true)
                        .build();

        when(
                credentialRepository.findByUserId(33L)
        ).thenReturn(Optional.of(credential));

        when(
                credentialRepository
                        .existsByEmailIgnoreCaseAndUserIdNot(
                                "nuevo@biblio.cl",
                                33L
                        )
        ).thenReturn(false);

        when(
                userClient.updateEmailInternally(
                        any(),
                        any(InternalEmailUpdateDTO.class)
                )
        ).thenReturn(
                updatedUser,
                restoredUser
        );

        when(
                credentialRepository.saveAndFlush(
                        any(AuthCredential.class)
                )
        ).thenThrow(
                new RuntimeException(
                        "Error en auth_db"
                )
        );

        assertThrows(
                EmailChangeProcessException.class,
                () -> authService.changeUserEmail(
                        33L,
                        requestDTO
                )
        );

        ArgumentCaptor<InternalEmailUpdateDTO> captor =
                ArgumentCaptor.forClass(
                        InternalEmailUpdateDTO.class
                );

        verify(
                userClient,
                times(2)
        ).updateEmailInternally(
                org.mockito.ArgumentMatchers.eq(33L),
                captor.capture()
        );

        List<InternalEmailUpdateDTO> requests =
                captor.getAllValues();

        assertEquals(
                "nuevo@biblio.cl",
                requests.get(0).getNewEmail()
        );

        assertEquals(
                "anterior@biblio.cl",
                requests.get(1).getNewEmail()
        );
    }

    @Test
    void changeUserEmail_shouldThrowProcessException_evenWhenCompensationFails() {

        ChangeEmailRequestDTO requestDTO =
                new ChangeEmailRequestDTO();

        requestDTO.setNewEmail(
                "nuevo@biblio.cl"
        );

        AuthCredential credential =
                AuthCredential.builder()
                        .id(54L)
                        .userId(34L)
                        .email("anterior@biblio.cl")
                        .passwordHash("HASH_BCRYPT")
                        .build();

        UserResponseDTO updatedUser =
                UserResponseDTO.builder()
                        .id(34L)
                        .name("Usuario")
                        .email("nuevo@biblio.cl")
                        .role("USER")
                        .active(true)
                        .build();

        when(
                credentialRepository.findByUserId(34L)
        ).thenReturn(Optional.of(credential));

        when(
                credentialRepository
                        .existsByEmailIgnoreCaseAndUserIdNot(
                                "nuevo@biblio.cl",
                                34L
                        )
        ).thenReturn(false);

        when(
                userClient.updateEmailInternally(
                        any(),
                        any(InternalEmailUpdateDTO.class)
                )
        ).thenReturn(updatedUser)
                .thenThrow(
                        new RuntimeException(
                                "Falló la compensación"
                        )
                );

        when(
                credentialRepository.saveAndFlush(
                        any(AuthCredential.class)
                )
        ).thenThrow(
                new RuntimeException(
                        "Error en auth_db"
                )
        );

        assertThrows(
                EmailChangeProcessException.class,
                () -> authService.changeUserEmail(
                        34L,
                        requestDTO
                )
        );

        verify(
                userClient,
                times(2)
        ).updateEmailInternally(
                org.mockito.ArgumentMatchers.eq(34L),
                any(InternalEmailUpdateDTO.class)
        );
    }
}
