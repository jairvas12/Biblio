package com.library.auth_service.client;

import com.library.auth_service.dto.UserRequestDTO;
import com.library.auth_service.dto.UserResponseDTO;
import com.library.auth_service.config.UserClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import com.library.auth_service.dto.InternalEmailUpdateDTO;

@FeignClient(
        name = "user-service",
        configuration = UserClientConfig.class
)
public interface UserClient {

    @PostMapping("/users")
    UserResponseDTO createUser(
            @RequestBody UserRequestDTO requestDTO
    );

    @GetMapping("/users/email/{email}")
    UserResponseDTO getUserByEmail(
            @PathVariable("email") String email
    );

    @DeleteMapping("/users/internal/{id}/deactivate")
    void deactivateUser(
            @PathVariable("id") Long id
    );
    
    @PutMapping("/users/internal/{id}/email")
    UserResponseDTO updateEmailInternally(
            @PathVariable("id") Long id,
            @RequestBody InternalEmailUpdateDTO requestDTO
    );
}
