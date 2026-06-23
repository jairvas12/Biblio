package com.library.user_service.controller;

import com.library.user_service.dto.UserRequestDTO;
import com.library.user_service.dto.UserResponseDTO;
import com.library.user_service.dto.UserUpdateDTO;
import com.library.user_service.service.UserService;

import com.library.user_service.dto.InternalEmailUpdateDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid @RequestBody UserRequestDTO requestDTO
    ) {

        UserResponseDTO createdUser =
                userService.createUser(requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdUser);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {

        return ResponseEntity.ok(
                userService.getAllUsers()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                userService.getUserById(id)
        );
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> getUserByEmail(
            @PathVariable String email
    ) {

        return ResponseEntity.ok(
                userService.getUserByEmail(email)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO requestDTO
    ) {

        UserResponseDTO updatedUser =
                userService.updateUser(
                        id,
                        requestDTO
                );

        return ResponseEntity.ok(updatedUser);
    }
    @PutMapping("/internal/{id}/email")
    public ResponseEntity<UserResponseDTO> updateEmailInternally(
            @PathVariable Long id,
            @Valid @RequestBody InternalEmailUpdateDTO requestDTO
    ) {

        UserResponseDTO updatedUser =
                userService.updateEmailInternally(
                        id,
                        requestDTO.getNewEmail()
                );

        return ResponseEntity.ok(updatedUser);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id
    ) {

        userService.deleteUser(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    @DeleteMapping("/internal/{id}/deactivate")
    public ResponseEntity<Void> deactivateUserInternally(
            @PathVariable Long id
    ) {

        userService.deleteUser(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}

