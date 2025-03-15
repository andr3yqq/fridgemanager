package com.example.smartfridge.controllers;


import com.example.smartfridge.dtos.UserDto;
import com.example.smartfridge.dtos.UserResponseDto;
import com.example.smartfridge.entities.User;
import com.example.smartfridge.services.AuthentificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/auth")
public class AuthentificationController {

    private final AuthentificationService authService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody UserDto userDto) {
        User registeredUser = authService.registerUser(userDto,passwordEncoder);

        UserResponseDto responseDto = new UserResponseDto(
                registeredUser.getId(),
                registeredUser.getUsername(),
                registeredUser.getEmail(),
                registeredUser.getRole(),
                registeredUser.getFridgeId() != null ? registeredUser.getFridgeId().getId() : null
        );

        return ResponseEntity.ok(responseDto);
    }


    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> login(@RequestBody UserDto userDto) {
        User user = authService.authenticate(userDto.getUsername(), userDto.getPassword(),passwordEncoder);

        UserResponseDto responseDto = new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getFridgeId() != null ? user.getFridgeId().getId() : null
        );

        return ResponseEntity.ok(responseDto);
    }

}
