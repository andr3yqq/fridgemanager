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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginReturnsMappedAuthenticationResponseForValidCredentials() {
        LoginRequestDto request = new LoginRequestDto("testuser", "testpassword");
        User user = user();
        AuthResponseDto expectedResponse = new AuthResponseDto();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtProvider.generateToken("testuser")).thenReturn("jwt-token");
        when(userMapper.toAuthResponseDto(user, "jwt-token")).thenReturn(expectedResponse);

        AuthResponseDto actualResponse = authService.login(request);

        assertSame(expectedResponse, actualResponse);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userMapper).toAuthResponseDto(user, "jwt-token");
    }

    @Test
    void loginPropagatesBadCredentialsException() {
        LoginRequestDto request = new LoginRequestDto("testuser", "wrong-password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verifyNoRegistrationInteractions();
    }

    @Test
    void registerNormalizesInputEncodesPasswordAndReturnsResponse() {
        RegisterRequestDto request = new RegisterRequestDto(
                "  testuser  ",
                "  TEST@EXAMPLE.COM  ",
                "testpassword"
        );
        AuthResponseDto expectedResponse = new AuthResponseDto();

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("testpassword")).thenReturn("encoded-password");
        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtProvider.generateToken("testuser")).thenReturn("jwt-token");
        when(userMapper.toAuthResponseDto(any(User.class), eq("jwt-token")))
                .thenReturn(expectedResponse);

        AuthResponseDto actualResponse = authService.register(request);

        assertSame(expectedResponse, actualResponse);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals(Role.USER, savedUser.getRole());
        assertTrue(savedUser.isEnabled());
        assertTrue(savedUser.isAccountNonLocked());
    }

    @Test
    void registerRejectsDuplicateUsername() {
        RegisterRequestDto request = new RegisterRequestDto(
                "testuser",
                "test@example.com",
                "testpassword"
        );

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).existsByEmail(any(String.class));
        verify(userRepository, never()).saveAndFlush(any(User.class));
        verifyNoRegistrationInteractions();
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequestDto request = new RegisterRequestDto(
                "testuser",
                "test@example.com",
                "testpassword"
        );

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).saveAndFlush(any(User.class));
        verifyNoRegistrationInteractions();
    }

    private User user() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setRole(Role.USER);
        return user;
    }

    private void verifyNoRegistrationInteractions() {
        verify(passwordEncoder, never()).encode(any(String.class));
        verify(jwtProvider, never()).generateToken(any(String.class));
        verify(userMapper, never()).toAuthResponseDto(any(User.class), any(String.class));
    }
}
