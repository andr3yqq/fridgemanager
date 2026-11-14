package com.example.smartfridge.services;

import com.example.smartfridge.dtos.ChangePasswordRequestDto;
import com.example.smartfridge.dtos.UserResponseDto;
import com.example.smartfridge.entities.User;
import com.example.smartfridge.exceptions.InvalidOldPasswordException;
import com.example.smartfridge.exceptions.PasswordReuseException;
import com.example.smartfridge.mappers.UserMapper;
import com.example.smartfridge.repositories.UserRepository;
import com.example.smartfridge.utils.UserUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTests {

    @Mock
    private UserUtils userUtils;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void getCurrentUserReturnsMappedAuthenticatedUser() {
        User user = user();
        UserResponseDto expectedResponse = new UserResponseDto(
                1L,
                "testuser",
                "test@example.com",
                "USER",
                null
        );

        when(userUtils.getUserFromAuthentication()).thenReturn(user);
        when(userMapper.toUserResponseDto(user)).thenReturn(expectedResponse);

        UserResponseDto actualResponse = accountService.getCurrentUser();

        assertSame(expectedResponse, actualResponse);
        verify(userUtils).getUserFromAuthentication();
        verify(userMapper).toUserResponseDto(user);
    }

    @Test
    void changePasswordUpdatesEncodedPasswordForValidOldPassword() {
        User user = user();
        ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                "old-password",
                "new-password"
        );

        when(userUtils.getUserFromAuthentication()).thenReturn(user);
        when(passwordEncoder.matches("old-password", "encoded-old-password"))
                .thenReturn(true);
        when(passwordEncoder.encode("new-password"))
                .thenReturn("encoded-new-password");

        accountService.changePassword(request);

        assertEquals("encoded-new-password", user.getPassword());
        verify(passwordEncoder).matches("old-password", "encoded-old-password");
        verify(passwordEncoder).encode("new-password");
        verify(userRepository).save(user);
    }

    @Test
    void changePasswordRejectsIncorrectOldPassword() {
        User user = user();
        ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                "wrong-password",
                "new-password"
        );

        when(userUtils.getUserFromAuthentication()).thenReturn(user);
        when(passwordEncoder.matches("wrong-password", "encoded-old-password"))
                .thenReturn(false);

        assertThrows(
                InvalidOldPasswordException.class,
                () -> accountService.changePassword(request)
        );

        verify(passwordEncoder, never()).encode(any(String.class));
        verify(userRepository, never()).save(any(User.class));
        assertEquals("encoded-old-password", user.getPassword());
    }

    @Test
    void changePasswordRejectsReusingOldPassword() {
        User user = user();
        ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                "same-password",
                "same-password"
        );

        when(userUtils.getUserFromAuthentication()).thenReturn(user);

        assertThrows(
                PasswordReuseException.class,
                () -> accountService.changePassword(request)
        );

        verify(passwordEncoder, never()).matches(any(String.class), any(String.class));
        verify(passwordEncoder, never()).encode(any(String.class));
        verify(userRepository, never()).save(any(User.class));
    }

    private User user() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encoded-old-password");
        return user;
    }
}
