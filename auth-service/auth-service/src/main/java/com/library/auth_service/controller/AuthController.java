package com.library.auth_service.controller;

import com.library.auth_service.dto.AuthResponseDTO;
import com.library.auth_service.dto.ChangeEmailRequestDTO;
import com.library.auth_service.dto.LoginRequestDTO;
import com.library.auth_service.dto.RegisterRequestDTO;
import com.library.auth_service.dto.UserResponseDTO;
import com.library.auth_service.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(
        name = "Autenticación",
        description = "Registro, inicio de sesión y administración de credenciales"
)
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Registrar una cuenta",
            description = """
                    Crea un usuario con rol USER en USER-SERVICE,
                    guarda sus credenciales en AUTH-SERVICE
                    y devuelve un token JWT.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Cuenta registrada correctamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = AuthResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de registro inválidos"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El correo ya está registrado"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "No fue posible completar el registro"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "USER-SERVICE no está disponible"
            )
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @Valid
            @RequestBody
            RegisterRequestDTO requestDTO
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        authService.register(
                                requestDTO
                        )
                );
    }

    @Operation(
            summary = "Iniciar sesión",
            description = """
                    Valida el correo y la contraseña,
                    comprueba que el usuario esté activo
                    y devuelve un token JWT.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inicio de sesión correcto",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = AuthResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de inicio de sesión inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Correo o contraseña incorrectos"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "La cuenta está desactivada"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "USER-SERVICE no está disponible"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid
            @RequestBody
            LoginRequestDTO requestDTO
    ) {

        return ResponseEntity.ok(
                authService.login(
                        requestDTO
                )
        );
    }

    @Operation(
            summary = "Cambiar el correo de un usuario",
            description = """
                    Actualiza coordinadamente el correo
                    en USER-SERVICE y AUTH-SERVICE.
                    Requiere un token JWT con rol ADMIN.
                    """,
            security = {
                    @SecurityRequirement(
                            name = "bearerAuth"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Correo actualizado correctamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = UserResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Nuevo correo inválido"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario autenticado no tiene rol ADMIN"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existen credenciales o el usuario no existe"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El correo ya pertenece a otra cuenta"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Falló el cambio coordinado de correo"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "USER-SERVICE no está disponible"
            )
    })
    @PatchMapping("/admin/users/{userId}/email")
    public ResponseEntity<UserResponseDTO> changeUserEmail(
            @Parameter(
                    name = "userId",
                    description = "Identificador del usuario",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "10"
            )
            @PathVariable
            Long userId,

            @Valid
            @RequestBody
            ChangeEmailRequestDTO requestDTO
    ) {

        UserResponseDTO updatedUser =
                authService.changeUserEmail(
                        userId,
                        requestDTO
                );

        return ResponseEntity.ok(
                updatedUser
        );
    }
}