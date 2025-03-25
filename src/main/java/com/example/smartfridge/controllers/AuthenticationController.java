package com.example.smartfridge.controllers;

import com.example.smartfridge.dtos.UserDto;
import com.example.smartfridge.dtos.UserResponseDto;
import com.example.smartfridge.entities.User;
import com.example.smartfridge.security.JwtProvider;
import com.example.smartfridge.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody UserDto userDto) {
        User registeredUser = authService.registerUser(userDto,passwordEncoder);

        UserResponseDto responseDto = new UserResponseDto(
                registeredUser.getId(),
                registeredUser.getUsername(),
                registeredUser.getEmail(),
                registeredUser.getRole(),
                registeredUser.getFridgeId() != null ? registeredUser.getFridgeId().getId() : null,
                jwtProvider.generateToken(registeredUser.getUsername())
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
                user.getFridgeId() != null ? user.getFridgeId().getId() : null,
                jwtProvider.generateToken(user.getUsername())
        );
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/login")
    public ResponseEntity<UserResponseDto> token(@RequestHeader("Authorization") String token) {
        User user = authService.tokenCheck();
        UserResponseDto responseDto = new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getFridgeId() != null ? user.getFridgeId().getId() : null,
                token.substring(7)
        );
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/update-password")
    public ResponseEntity<UserResponseDto> changePassword(@RequestBody Map<String, String> requestBody) {
        User user = authService.changePassword(requestBody.get("oldPassword"),requestBody.get("newPassword"),passwordEncoder);
        UserResponseDto responseDto = new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getFridgeId() != null ? user.getFridgeId().getId() : null,
                jwtProvider.generateToken(user.getUsername())
        );
        return ResponseEntity.ok(responseDto);
    }
}
