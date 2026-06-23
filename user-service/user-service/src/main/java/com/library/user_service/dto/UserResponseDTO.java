package com.library.user_service.dto;

import com.library.user_service.model.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDTO {

    private Long id;

    private String name;

    private String email;

    private Role role;

    private Boolean active;
}