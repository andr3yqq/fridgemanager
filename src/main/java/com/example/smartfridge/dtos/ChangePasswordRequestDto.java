package com.example.smartfridge.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDto(
        @NotBlank String oldPassword,
        @NotBlank @Size(min = 8, max = 30) String newPassword) {
}
