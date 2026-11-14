package com.example.smartfridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuthResponseDto {
    private Long id;
    private String username;
    private String email;
    private String role;
    private Long fridgeId;
    private String token;

    public AuthResponseDto(UserResponseDto userResponseDto, String token) {
        this.id = userResponseDto.id();
        this.username = userResponseDto.username();
        this.email = userResponseDto.email();
        this.role = userResponseDto.role();
        this.fridgeId = userResponseDto.fridgeId();
        this.token = token;
    }
}
