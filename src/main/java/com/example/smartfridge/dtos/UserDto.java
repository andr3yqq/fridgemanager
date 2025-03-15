package com.example.smartfridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String password;
    private String role;
    private Long fridgeId;
}
