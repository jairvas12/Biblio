package com.library.auth_service.bootstrap;

import com.library.auth_service.client.UserClient;
import com.library.auth_service.dto.UserRequestDTO;
import com.library.auth_service.dto.UserResponseDTO;
import com.library.auth_service.model.AuthCredential;
import com.library.auth_service.repository.AuthCredentialRepository;

import feign.FeignException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapTest {

    @Mock
    private UserClient userClient;

    @Mock
    private AuthCredentialRepository credentialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminBootstrap adminBootstrap;

    @BeforeEach
    void setUp() {

        configureBootstrap(
                true,
                "Administrador Principal",
                "admin@biblio.cl",
                "ClaveAdmin123"
        );
    }

    @Test
    void run_shouldDoNothing_whenBootstrapIsDisabled() {

        configureBootstrap(
                false,
                "Administrador",
                "admin@biblio.cl",
                "ClaveAdmin123"
        );

        adminBootstrap.run(null);

        verifyNoInteractions(
                userClient,
                credentialRepository,
                passwordEncoder
        );
    }

    @Test
    void run_shouldStop_whenAdminNameIsMissing() {

        configureBootstrap(
                true,
                null,
                "admin@biblio.cl",
                "ClaveAdmin123"
        );

        adminBootstrap.run(null);

        verifyNoInteractions(
                userClient,
                credentialRepository,
                passwordEncoder
        );
    }

    @Test
    void run_shouldStop_whenAdminEmailIsMissing() {

        configureBootstrap(
                true,
                "Administrador",
                null,
                "ClaveAdmin123"
        );

        adminBootstrap.run(null);

        verifyNoInteractions(
                userClient,
                credentialRepository,
                passwordEncoder
        );
    }

    @Test
    void run_shouldStop_whenAdminPasswordIsNull() {

        configureBootstrap(
                true,
                "Administrador",
                "admin@biblio.cl",
                null
        );

        adminBootstrap.run(null);

        verifyNoInteractions(
                userClient,
                credentialRepository,
                passwordEncoder
        );
    }

    @Test
    void run_shouldStop_whenAdminPasswordIsBlank() {

        configureBootstrap(
                true,
                "Administrador",
                "admin@biblio.cl",
                "   "
        );

        adminBootstrap.run(null);

        verifyNoInteractions(
                userClient,
                credentialRepository,
                passwordEncoder
        );
    }

    @Test
    void run_shouldStop_whenPasswordDoesNotMeetRequirements() {

        configureBootstrap(
                true,
                "Administrador",
                "admin@biblio.cl",
                "clavesimple"
        );

        adminBootstrap.run(null);

        verifyNoInteractions(
                userClient,
                credentialRepository,
                passwordEncoder
        );
    }

    @Test
    void run_shouldStop_whenCredentialAlreadyExists() {

        when(
                credentialRepository.existsByEmailIgnoreCase(
                        "admin@biblio.cl"
                )
        ).thenReturn(true);

        adminBootstrap.run(null);

        verify(
                credentialRepository,
                never()
        ).save(any(AuthCredential.class));

        verifyNoInteractions(
                userClient,
                passwordEncoder
        );
    }

    @Test
    void run_shouldStop_whenExistingUserIsNotAdmin() {

        when(
                credentialRepository.existsByEmailIgnoreCase(
                        "admin@biblio.cl"
                )
        ).thenReturn(false);

        UserResponseDTO existingUser =
                UserResponseDTO.builder()
                        .id(1L)
                        .name("Usuario Existente")
                        .email("admin@biblio.cl")
                        .role("USER")
                        .active(true)
                        .build();

        when(
                userClient.getUserByEmail(
                        "admin@biblio.cl"
                )
        ).thenReturn(existingUser);

        adminBootstrap.run(null);

        verify(
                credentialRepository,
                never()
        ).save(any(AuthCredential.class));

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void run_shouldStop_whenExistingAdminIsInactive() {

        when(
                credentialRepository.existsByEmailIgnoreCase(
                        "admin@biblio.cl"
                )
        ).thenReturn(false);

        UserResponseDTO inactiveAdmin =
                UserResponseDTO.builder()
                        .id(2L)
                        .name("Administrador Inactivo")
                        .email("admin@biblio.cl")
                        .role("ADMIN")
                        .active(false)
                        .build();

        when(
                userClient.getUserByEmail(
                        "admin@biblio.cl"
                )
        ).thenReturn(inactiveAdmin);

        adminBootstrap.run(null);

        verify(
                credentialRepository,
                never()
        ).save(any(AuthCredential.class));

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void run_shouldCreateCredentialForExistingActiveAdmin() {

        when(
                credentialRepository.existsByEmailIgnoreCase(
                        "admin@biblio.cl"
                )
        ).thenReturn(false);

        UserResponseDTO existingAdmin =
                UserResponseDTO.builder()
                        .id(3L)
                        .name("Administrador")
                        .email("admin@biblio.cl")
                        .role("ADMIN")
                        .active(true)
                        .build();

        when(
                userClient.getUserByEmail(
                        "admin@biblio.cl"
                )
        ).thenReturn(existingAdmin);

        when(
                passwordEncoder.encode(
                        "ClaveAdmin123"
                )
        ).thenReturn("HASH_ADMIN");

        adminBootstrap.run(null);

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
                "admin@biblio.cl",
                savedCredential.getEmail()
        );

        assertEquals(
                "HASH_ADMIN",
                savedCredential.getPasswordHash()
        );

        verify(
                userClient,
                never()
        ).createUser(any(UserRequestDTO.class));

        verify(
                userClient,
                never()
        ).deactivateUser(any());
    }

    @Test
    void run_shouldCreateUserAndCredential_whenAdminDoesNotExist() {

        configureBootstrap(
                true,
                "  Administrador   Principal  ",
                " ADMIN@BIBLIO.CL ",
                "ClaveAdmin123"
        );

        when(
                credentialRepository.existsByEmailIgnoreCase(
                        "admin@biblio.cl"
                )
        ).thenReturn(false);

        FeignException.NotFound notFound =
                mock(FeignException.NotFound.class);

        when(
                userClient.getUserByEmail(
                        "admin@biblio.cl"
                )
        ).thenThrow(notFound);

        UserResponseDTO createdAdmin =
                UserResponseDTO.builder()
                        .id(4L)
                        .name("Administrador Principal")
                        .email("admin@biblio.cl")
                        .role("ADMIN")
                        .active(true)
                        .build();

        when(
                userClient.createUser(
                        any(UserRequestDTO.class)
                )
        ).thenReturn(createdAdmin);

        when(
                passwordEncoder.encode(
                        "ClaveAdmin123"
                )
        ).thenReturn("HASH_ADMIN");

        adminBootstrap.run(null);

        ArgumentCaptor<UserRequestDTO> userCaptor =
                ArgumentCaptor.forClass(
                        UserRequestDTO.class
                );

        verify(userClient).createUser(
                userCaptor.capture()
        );

        UserRequestDTO sentRequest =
                userCaptor.getValue();

        assertEquals(
                "Administrador Principal",
                sentRequest.getName()
        );

        assertEquals(
                "admin@biblio.cl",
                sentRequest.getEmail()
        );

        assertEquals(
                "ADMIN",
                sentRequest.getRole()
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
                4L,
                savedCredential.getUserId()
        );

        assertEquals(
                "admin@biblio.cl",
                savedCredential.getEmail()
        );

        assertEquals(
                "HASH_ADMIN",
                savedCredential.getPasswordHash()
        );
    }

    @Test
    void run_shouldStop_whenUserServiceIsUnavailable() {

        when(
                credentialRepository.existsByEmailIgnoreCase(
                        "admin@biblio.cl"
                )
        ).thenReturn(false);

        FeignException feignException =
                mock(FeignException.class);

        when(
                feignException.status()
        ).thenReturn(503);

        when(
                userClient.getUserByEmail(
                        "admin@biblio.cl"
                )
        ).thenThrow(feignException);

        assertDoesNotThrow(
                () -> adminBootstrap.run(null)
        );

        verify(
                credentialRepository,
                never()
        ).save(any(AuthCredential.class));

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void run_shouldNotDeactivateExistingAdmin_whenCredentialSaveFails() {

        when(
                credentialRepository.existsByEmailIgnoreCase(
                        "admin@biblio.cl"
                )
        ).thenReturn(false);

        UserResponseDTO existingAdmin =
                UserResponseDTO.builder()
                        .id(5L)
                        .name("Administrador Existente")
                        .email("admin@biblio.cl")
                        .role("ADMIN")
                        .active(true)
                        .build();

        when(
                userClient.getUserByEmail(
                        "admin@biblio.cl"
                )
        ).thenReturn(existingAdmin);

        when(
                passwordEncoder.encode(
                        "ClaveAdmin123"
                )
        ).thenReturn("HASH_ADMIN");

        when(
                credentialRepository.save(
                        any(AuthCredential.class)
                )
        ).thenThrow(
                new RuntimeException(
                        "Error en auth_db"
                )
        );

        assertDoesNotThrow(
                () -> adminBootstrap.run(null)
        );

        verify(
                userClient,
                never()
        ).deactivateUser(any());
    }

    @Test
    void run_shouldDeactivateCreatedAdmin_whenCredentialSaveFails() {

        when(
                credentialRepository.existsByEmailIgnoreCase(
                        "admin@biblio.cl"
                )
        ).thenReturn(false);

        FeignException.NotFound notFound =
                mock(FeignException.NotFound.class);

        when(
                userClient.getUserByEmail(
                        "admin@biblio.cl"
                )
        ).thenThrow(notFound);

        UserResponseDTO createdAdmin =
                UserResponseDTO.builder()
                        .id(6L)
                        .name("Administrador")
                        .email("admin@biblio.cl")
                        .role("ADMIN")
                        .active(true)
                        .build();

        when(
                userClient.createUser(
                        any(UserRequestDTO.class)
                )
        ).thenReturn(createdAdmin);

        when(
                passwordEncoder.encode(
                        "ClaveAdmin123"
                )
        ).thenReturn("HASH_ADMIN");

        when(
                credentialRepository.save(
                        any(AuthCredential.class)
                )
        ).thenThrow(
                new RuntimeException(
                        "Error en auth_db"
                )
        );

        assertDoesNotThrow(
                () -> adminBootstrap.run(null)
        );

        verify(userClient).deactivateUser(
                6L
        );
    }

    @Test
    void run_shouldFinishEvenWhenCompensationFails() {

        when(
                credentialRepository.existsByEmailIgnoreCase(
                        "admin@biblio.cl"
                )
        ).thenReturn(false);

        FeignException.NotFound notFound =
                mock(FeignException.NotFound.class);

        when(
                userClient.getUserByEmail(
                        "admin@biblio.cl"
                )
        ).thenThrow(notFound);

        UserResponseDTO createdAdmin =
                UserResponseDTO.builder()
                        .id(7L)
                        .name("Administrador")
                        .email("admin@biblio.cl")
                        .role("ADMIN")
                        .active(true)
                        .build();

        when(
                userClient.createUser(
                        any(UserRequestDTO.class)
                )
        ).thenReturn(createdAdmin);

        when(
                passwordEncoder.encode(
                        "ClaveAdmin123"
                )
        ).thenReturn("HASH_ADMIN");

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
                        "Error desactivando usuario"
                )
        ).when(userClient)
                .deactivateUser(7L);

        assertDoesNotThrow(
                () -> adminBootstrap.run(null)
        );

        verify(userClient).deactivateUser(
                7L
        );
    }

    private void configureBootstrap(
            boolean enabled,
            String name,
            String email,
            String password
    ) {

        ReflectionTestUtils.setField(
                adminBootstrap,
                "bootstrapEnabled",
                enabled
        );

        ReflectionTestUtils.setField(
                adminBootstrap,
                "adminName",
                name
        );

        ReflectionTestUtils.setField(
                adminBootstrap,
                "adminEmail",
                email
        );

        ReflectionTestUtils.setField(
                adminBootstrap,
                "adminPassword",
                password
        );
    }
}
