package com.library.auth_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDTO {

    private String token;

    private String tokenType;

    private Long userId;

    private String email;

    private String role;
}
