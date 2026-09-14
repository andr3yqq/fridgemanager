package com.example.smartfridge.controllers;

import com.example.smartfridge.dtos.UserDto;
import com.example.smartfridge.dtos.UserResponseDto;
import com.example.smartfridge.entities.User;
import com.example.smartfridge.security.JwtProvider;
import com.example.smartfridge.services.AuthenticationService;
import com.example.smartfridge.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authService;
    private final JwtProvider jwtProvider;
    private final UserUtils userUtils;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody UserDto userDto) {
        User registeredUser = authService.registerUser(userDto);

        UserResponseDto responseDto = new UserResponseDto(
                registeredUser.getId(),
                registeredUser.getUsername(),
                registeredUser.getEmail(),
                registeredUser.getRole(),
                registeredUser.getFridge() != null ? registeredUser.getFridge().getId() : null,
                jwtProvider.generateToken(registeredUser.getUsername())
        );

        return ResponseEntity.ok(responseDto);
    }


    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> login(@RequestBody UserDto userDto) {
        User user = authService.authenticate(userDto.getUsername(), userDto.getPassword());

        UserResponseDto responseDto = new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getFridge() != null ? user.getFridge().getId() : null,
                jwtProvider.generateToken(user.getUsername())
        );
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/login")
    public ResponseEntity<UserResponseDto> token(@RequestHeader("Authorization") String token) {
        User user = userUtils.getUserFromAuthentication();
        UserResponseDto responseDto = new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getFridge() != null ? user.getFridge().getId() : null,
                token.substring(7)
        );
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/update-password")
    public ResponseEntity<UserResponseDto> changePassword(@RequestBody Map<String, String> requestBody) {
        User user = authService.changePassword(requestBody.get("oldPassword"), requestBody.get("newPassword"));
        UserResponseDto responseDto = new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getFridge() != null ? user.getFridge().getId() : null,
                jwtProvider.generateToken(user.getUsername())
        );
        return ResponseEntity.ok(responseDto);
    }
}
