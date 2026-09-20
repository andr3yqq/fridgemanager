package com.example.smartfridge.dtos;

public record UserResponseDto(
        Long id,
        String username,
        String email,
        String role,
        Long fridgeId
) {
}
