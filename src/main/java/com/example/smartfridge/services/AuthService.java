package com.example.smartfridge.services;

import com.example.smartfridge.dtos.AuthResponseDto;
import com.example.smartfridge.dtos.LoginRequestDto;
import com.example.smartfridge.dtos.RegisterRequestDto;
import com.example.smartfridge.entities.User;
import com.example.smartfridge.enums.Role;
import com.example.smartfridge.exceptions.UserAlreadyExistsException;
import com.example.smartfridge.mappers.UserMapper;
import com.example.smartfridge.repositories.UserRepository;
import com.example.smartfridge.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    public AuthResponseDto login(LoginRequestDto request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        User user = (User) auth.getPrincipal();
        return userMapper.toAuthResponseDto(user, jwtProvider.generateToken(user.getUsername()));
    }

    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username is already taken.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email is already taken.");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .role(Role.USER)
                .build();
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.saveAndFlush(user);

        return userMapper.toAuthResponseDto(user, jwtProvider.generateToken(user.getUsername()));
    }

}
