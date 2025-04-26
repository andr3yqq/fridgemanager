package com.example.smartfridge.services;

import com.example.smartfridge.dtos.UserDto;
import com.example.smartfridge.entities.Fridge;
import com.example.smartfridge.entities.User;
import com.example.smartfridge.mappers.UserMapper;
import com.example.smartfridge.repositories.FridgeRepository;
import com.example.smartfridge.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FridgeRepository fridgeRepository;

    public UserDto findUserByEmail(String email) {
        User user = userRepository.findUserByEmail(email).orElse(null);
        return userMapper.toUserDto(user);
    }

    public UserDto findUserByUsername(String username) {
        User user = userRepository.findUserByUsername(username).orElse(null);
        return userMapper.toUserDto(user);
    }

    public UserDto findUserById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        return userMapper.toUserDto(user);
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User authenticate(String username, String rawPassword, PasswordEncoder passwordEncoder) {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new UsernameNotFoundException("Invalid credentials");
        }

        return user;
    }

    public User changePassword(String oldPassword, String newPassword, PasswordEncoder passwordEncoder) {
        User user = tokenCheck();
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new UsernameNotFoundException("Invalid credentials");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    public User tokenCheck() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User registerUser(UserDto userDto, PasswordEncoder passwordEncoder) {
        if (userRepository.findUserByUsername(userDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already taken.");
        }
        if (userRepository.findUserByEmail(userDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already taken.");
        }

        Fridge fridge = fridgeRepository.findById(0L).orElse(null);
        User newUser = new User(
                null,
                userDto.getUsername(),
                userDto.getEmail(),
                passwordEncoder.encode(userDto.getPassword()),
                userDto.getRole(),
                fridge
        );

        return userRepository.save(newUser);
    }

}
