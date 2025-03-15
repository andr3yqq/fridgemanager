package com.example.smartfridge.services;

import com.example.smartfridge.dtos.UserDto;
import com.example.smartfridge.entities.Fridge;
import com.example.smartfridge.entities.User;
import com.example.smartfridge.mappers.UserMapper;
import com.example.smartfridge.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthentificationService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

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

    public User registerUser(UserDto userDto, PasswordEncoder passwordEncoder) {
        // Check if username or email already exists
        if (userRepository.findUserByUsername(userDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already taken.");
        }
        if (userRepository.findUserByEmail(userDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already taken.");
        }

        Fridge fridge = new Fridge(0L, "Basic", null);
        /*if (userDto.getFridgeId() != null) {
            fridge = fridgeRepository.findById(userDto.getFridgeId())
                    .orElseThrow(() -> new IllegalArgumentException("Fridge not found."));
        }*/

        // Create and save user
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
