package com.example.smartfridge.services;

import com.example.smartfridge.dtos.ChangePasswordRequestDto;
import com.example.smartfridge.dtos.UserResponseDto;
import com.example.smartfridge.entities.User;
import com.example.smartfridge.exceptions.InvalidOldPasswordException;
import com.example.smartfridge.exceptions.PasswordReuseException;
import com.example.smartfridge.mappers.UserMapper;
import com.example.smartfridge.repositories.UserRepository;
import com.example.smartfridge.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserUtils userUtils;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUser() {
        User user = userUtils.getUserFromAuthentication();
        return userMapper.toUserResponseDto(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequestDto request) {
        User user = userUtils.getUserFromAuthentication();
        String newPassword = request.newPassword();
        String oldPassword = request.oldPassword();
        if (newPassword.equals(oldPassword)) {
            throw new PasswordReuseException("New password cannot be the same as the old password.");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidOldPasswordException("Old password is incorrect.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}