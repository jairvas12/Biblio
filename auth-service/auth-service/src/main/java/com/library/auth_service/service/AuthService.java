package com.library.auth_service.service;

import com.library.auth_service.dto.AuthResponseDTO;
import com.library.auth_service.dto.ChangeEmailRequestDTO;
import com.library.auth_service.dto.LoginRequestDTO;
import com.library.auth_service.dto.RegisterRequestDTO;
import com.library.auth_service.dto.UserResponseDTO;

public interface AuthService {

    AuthResponseDTO register(
            RegisterRequestDTO requestDTO
    );

    AuthResponseDTO login(
            LoginRequestDTO requestDTO
    );

    UserResponseDTO changeUserEmail(
            Long userId,
            ChangeEmailRequestDTO requestDTO
    );
}