package com.example.storyloom_auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "User name required")
    private String username;
    private String password;
}
