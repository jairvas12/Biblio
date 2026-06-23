package com.library.user_service.service;

import com.library.user_service.dto.UserRequestDTO;
import com.library.user_service.dto.UserResponseDTO;
import com.library.user_service.dto.UserUpdateDTO;

import java.util.List;

public interface UserService {

    UserResponseDTO createUser(
            UserRequestDTO requestDTO
    );

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserById(
            Long id
    );

    UserResponseDTO getUserByEmail(
            String email
    );

    UserResponseDTO updateUser(
            Long id,
            UserUpdateDTO requestDTO
    );

    UserResponseDTO updateEmailInternally(
            Long id,
            String newEmail
    );

    void deleteUser(
            Long id
    );
}