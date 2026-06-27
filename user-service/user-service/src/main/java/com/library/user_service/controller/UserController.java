package com.library.user_service.controller;

import com.library.user_service.dto.InternalEmailUpdateDTO;
import com.library.user_service.dto.UserRequestDTO;
import com.library.user_service.dto.UserResponseDTO;
import com.library.user_service.dto.UserUpdateDTO;
import com.library.user_service.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(
        name = "Usuarios",
        description = """
                Administración de perfiles de usuario,
                roles, estado y sincronización interna con AUTH.
                """
)
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(
            summary = "Crear perfil de usuario",
            description = """
                    Crea el perfil asociado a una cuenta registrada
                    desde el microservicio AUTH.
                    Esta operación es exclusivamente interna.
                    """,
            security = @SecurityRequirement(
                    name = "internalApiKey"
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario creado correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "API Key interna inválida o ausente"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El correo ya está registrado"
            )
    })
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid
            @RequestBody
            UserRequestDTO requestDTO
    ) {

        UserResponseDTO createdUser =
                userService.createUser(requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdUser);
    }

    @GetMapping
    @Operation(
            summary = "Listar todos los usuarios",
            description = """
                    Obtiene todos los perfiles registrados.
                    Disponible para ADMIN y BIBLIOTECARIO.
                    """,
            security = @SecurityRequirement(
                    name = "bearerAuth"
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuarios encontrados"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT ausente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El rol no posee permisos"
            )
    })
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {

        return ResponseEntity.ok(
                userService.getAllUsers()
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar usuario por ID",
            description = """
                    Obtiene un perfil mediante su identificador.
                    Disponible para ADMIN y BIBLIOTECARIO.
                    """,
            security = @SecurityRequirement(
                    name = "bearerAuth"
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario encontrado"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT ausente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El rol no posee permisos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            )
    })
    public ResponseEntity<UserResponseDTO> getUserById(
            @Parameter(
                    description = "Identificador del usuario",
                    example = "3"
            )
            @PathVariable
            Long id
    ) {

        return ResponseEntity.ok(
                userService.getUserById(id)
        );
    }

    @GetMapping("/email/{email}")
    @Operation(
            summary = "Buscar usuario por correo",
            description = """
                    Consulta interna utilizada principalmente por AUTH
                    durante el login y otras operaciones coordinadas.
                    """,
            security = @SecurityRequirement(
                    name = "internalApiKey"
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario encontrado"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "API Key interna inválida o ausente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            )
    })
    public ResponseEntity<UserResponseDTO> getUserByEmail(
            @Parameter(
                    description = "Correo electrónico del usuario",
                    example = "usuario@biblio.cl"
            )
            @PathVariable
            String email
    ) {

        return ResponseEntity.ok(
                userService.getUserByEmail(email)
        );
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar nombre y rol",
            description = """
                    Permite a un ADMIN actualizar el nombre y el rol.
                    El correo no puede modificarse mediante esta ruta.
                    """,
            security = @SecurityRequirement(
                    name = "bearerAuth"
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario actualizado correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos, rol o cambio de correo inválido"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT ausente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "La operación requiere rol ADMIN"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            )
    })
    public ResponseEntity<UserResponseDTO> updateUser(
            @Parameter(
                    description = "Identificador del usuario",
                    example = "3"
            )
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            UserUpdateDTO requestDTO
    ) {

        UserResponseDTO updatedUser =
                userService.updateUser(
                        id,
                        requestDTO
                );

        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/internal/{id}/email")
    @Operation(
            summary = "Actualizar correo internamente",
            description = """
                    Actualiza el correo de USER durante una operación
                    coordinada iniciada por AUTH.
                    """,
            security = @SecurityRequirement(
                    name = "internalApiKey"
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Correo actualizado correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Correo inválido"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "API Key interna inválida o ausente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El correo pertenece a otro usuario"
            )
    })
    public ResponseEntity<UserResponseDTO> updateEmailInternally(
            @Parameter(
                    description = "Identificador del usuario",
                    example = "3"
            )
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            InternalEmailUpdateDTO requestDTO
    ) {

        UserResponseDTO updatedUser =
                userService.updateEmailInternally(
                        id,
                        requestDTO.getNewEmail()
                );

        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Desactivar usuario",
            description = """
                    Realiza una desactivación lógica.
                    El registro no se elimina físicamente.
                    Requiere rol ADMIN.
                    """,
            security = @SecurityRequirement(
                    name = "bearerAuth"
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Usuario desactivado correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT ausente o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "La operación requiere rol ADMIN"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            )
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(
                    description = "Identificador del usuario",
                    example = "3"
            )
            @PathVariable
            Long id
    ) {

        userService.deleteUser(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    @DeleteMapping("/internal/{id}/deactivate")
    @Operation(
            summary = "Compensar registro fallido",
            description = """
                    Desactiva internamente un usuario cuando AUTH
                    no logra completar el registro de sus credenciales.
                    """,
            security = @SecurityRequirement(
                    name = "internalApiKey"
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Usuario desactivado correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "API Key interna inválida o ausente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            )
    })
    public ResponseEntity<Void> deactivateUserInternally(
            @Parameter(
                    description = "Identificador del usuario",
                    example = "3"
            )
            @PathVariable
            Long id
    ) {

        userService.deleteUser(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}