package com.library.auth_service.controller;

import com.library.auth_service.dto.*;

import com.library.auth_service.service.AuthService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController

@RequestMapping("/auth")

@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(

            @Valid @RequestBody
            RegisterRequestDTO requestDTO) {

        return ResponseEntity.status(HttpStatus.CREATED)

                .body(authService.register(requestDTO));

    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(

            @Valid @RequestBody
            LoginRequestDTO requestDTO) {

        return ResponseEntity.ok(
                authService.login(requestDTO)
        );

    }
    @PatchMapping("/admin/users/{userId}/email")
    public ResponseEntity<UserResponseDTO> changeUserEmail(
            @PathVariable Long userId,
            @Valid @RequestBody ChangeEmailRequestDTO requestDTO
    ) {

        UserResponseDTO updatedUser =
                authService.changeUserEmail(
                        userId,
                        requestDTO
                );

        return ResponseEntity.ok(updatedUser);
    }
}