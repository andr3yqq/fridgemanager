package com.example.smartfridge.services;

import com.example.smartfridge.dtos.UserDto;
import com.example.smartfridge.entities.User;
import com.example.smartfridge.mappers.UserMapper;
import com.example.smartfridge.repositories.UserRepository;
import com.example.smartfridge.utils.UserUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTests {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserUtils userUtils;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("testpassword"));
        testUser.setEmail("testemail@example.com");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        testUser = null;
    }

    @Test
    void shouldAuthenticateUser_GivenValidCredentials() {
        String username = "testuser";
        String rawPassword = "testpassword";

        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(rawPassword, testUser.getPassword())).thenReturn(true);

        User result = authenticationService.authenticate(username, rawPassword);
        assertEquals(testUser, result);
    }

    @Test
    void shouldNotAuthenticateUser_GivenInvalidCredentials() {
        String username = "testuser";
        String rawPassword = "invalid";

        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(testUser));
        when(!passwordEncoder.matches(rawPassword, testUser.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authenticationService.authenticate(username, rawPassword));
    }

    @Test
    void shouldThrowException_GivenNonExistentUser() {
        String username = "nonexistentuser";
        String rawPassword = "testpassword";

        when(userRepository.findUserByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authenticationService.authenticate(username, rawPassword));
    }

    @Test
    void shouldChangePassword_GivenValidCredentials() {
        String rawPassword = "testpassword";
        String newPassword = "newpassword";

        when(userUtils.getUserFromAuthentication()).thenReturn(testUser);
        when(passwordEncoder.matches(rawPassword, testUser.getPassword())).thenReturn(true);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(passwordEncoder.encode(newPassword)).thenReturn(newPassword);

        User user = authenticationService.changePassword(rawPassword, newPassword);

        verify(userRepository).save(testUser);
        verify(passwordEncoder).encode(newPassword);
        assertEquals(newPassword, user.getPassword());
    }

    @Test
    void shouldThrowException_OnPasswordChange_GivenInvalidCredentials() {
        String rawPassword = "wrongpassword";
        String newPassword = "newpassword";

        when(userUtils.getUserFromAuthentication()).thenReturn(testUser);
        when(passwordEncoder.matches(rawPassword, testUser.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authenticationService.changePassword(rawPassword, newPassword));
    }

    @Test
    void shouldRegisterUser_GivenValidDetails() {
        UserDto userDto = new UserDto(
                "testuser",
                "testpassword",
                "testemail@example.com"
        );

        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toUser(userDto)).thenReturn(testUser);

        User user = authenticationService.registerUser(userDto);

        verify(userRepository).save(any(User.class));
        assertEquals(userDto.getUsername(), user.getUsername());
        assertEquals(userDto.getEmail(), user.getEmail());
    }


}
