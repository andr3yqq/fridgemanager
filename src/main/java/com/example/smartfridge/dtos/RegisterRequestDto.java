package com.example.smartfridge.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank @Size(min = 3, max = 25) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 30) String password) {
}
